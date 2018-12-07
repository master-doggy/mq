/*
 * Copyright 2018 Qunar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.com.qunar.pay.trade.api.card.service.usercard.UserCardQueryFacade
 */

package qunar.tc.qmq.delay.receiver;

import com.google.common.base.CharMatcher;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qmq.base.MessageHeader;
import qunar.tc.qmq.configuration.BrokerConfig;
import qunar.tc.qmq.configuration.DynamicConfig;
import qunar.tc.qmq.delay.DelayLogFacade;
import qunar.tc.qmq.delay.base.ReceivedDelayMessage;
import qunar.tc.qmq.delay.base.ReceivedResult;
import qunar.tc.qmq.delay.monitor.QMon;
import qunar.tc.qmq.delay.receiver.filter.OverDelayException;
import qunar.tc.qmq.delay.receiver.filter.ReceiveFilterChain;
import qunar.tc.qmq.delay.store.model.RawMessageExtend;
import qunar.tc.qmq.protocol.*;
import qunar.tc.qmq.protocol.producer.MessageProducerCode;
import qunar.tc.qmq.sync.DelaySyncRequest;
import qunar.tc.qmq.util.RemotingBuilder;
import qunar.tc.qmq.utils.CharsetUtils;
import qunar.tc.qmq.utils.RetrySubjectUtils;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

import static qunar.tc.qmq.delay.receiver.filter.OverDelayFilter.TWO_YEAR_MILLIS;

/**
 * @author xufeng.deng dennisdxf@gmail.com
 * @since 2018-07-26 14:46
 */
public class Receiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private final DelayLogFacade facade;
    private final DynamicConfig config;
    private final Invoker invoker;
    private final Deque<ReceiveEntry> waitSlaveSyncQueue = new LinkedBlockingDeque<>();

    public Receiver(DynamicConfig config, DelayLogFacade facade) {
        this.config = config;
        this.facade = facade;
        this.invoker = new ReceiveFilterChain().buildFilterChain(this::doInvoke);
    }

    ListenableFuture<Datagram> receive(List<RawMessageExtend> messages, RemotingCommand cmd) {
        final List<SettableFuture<ReceivedResult>> futures = new ArrayList<>(messages.size());
        for (RawMessageExtend message : messages) {
            final MessageHeader header = message.getHeader();
            monitorMessageReceived(header.getCreateTime(), header.getSubject());

            final ReceivedDelayMessage receivedDelayMessage = new ReceivedDelayMessage(message, cmd.getReceiveTime());
            futures.add(receivedDelayMessage.getPromise());
            try {
                invoker.invoke(receivedDelayMessage);
            } catch (OverDelayException e) {
                overDelay(receivedDelayMessage);
            }
        }

        final short version = cmd.getHeader().getVersion();
        return Futures.transform(Futures.allAsList(futures)
                , results -> RemotingBuilder.buildResponseDatagram(CommandCode.SUCCESS
                        , cmd.getHeader(), new SendResultPayloadHolder(results, version)));
    }

    private void doInvoke(ReceivedDelayMessage message) {
        if (BrokerConfig.isReadonly()) {
            brokerReadOnly(message);
            return;
        }

        if (bigSlaveLag()) {
            brokerReadOnly(message);
            return;
        }

        if (isSubjectIllegal(message.getSubject())) {
            if (allowRejectIllegalSubject()) {
                notAllowed(message);
                return;
            }
        }

        try {
            ReceivedResult result = facade.appendMessageLog(message);
            offer(message, result);
        } catch (Throwable t) {
            error(message, t);
        }
    }

    private void monitorMessageReceived(long receiveTime, String subject) {
        if (RetrySubjectUtils.isRetrySubject(subject) || RetrySubjectUtils.isDeadRetrySubject(subject)) {
            QMon.receivedRetryMessagesCountInc(subject);
        }
        QMon.receivedMessagesCountInc(subject);
        QMon.produceTime(subject, System.currentTimeMillis() - receiveTime);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void syncRequest(DelaySyncRequest syncRequest) {
        long messageLogOffset = syncRequest.getMessageLogOffset();
        ReceiveEntry first;
        while ((first = this.waitSlaveSyncQueue.peekFirst()) != null) {
            if (first.result.getMessageLogOffset() > messageLogOffset) {
                break;
            }

            this.waitSlaveSyncQueue.pop();
            end(first.message, first.result);
        }
    }

    private void notAllowed(ReceivedDelayMessage message) {
        QMon.rejectReceivedMessageCountInc(message.getSubject());
        end(message, new ReceivedResult(message.getMessageId(), MessageProducerCode.SUBJECT_NOT_ASSIGNED, "message rejected"));
    }

    private boolean isSubjectIllegal(final String subject) {
        final String trimmedSubject = CharMatcher.INVISIBLE.trimFrom(subject);
        if (Objects.equals(subject, trimmedSubject)) {
            return false;
        }

        QMon.receivedIllegalSubjectMessagesCountInc(trimmedSubject);
        LOGGER.error("received message with illegal subject. subject: {}", subject);
        return true;
    }

    private boolean allowRejectIllegalSubject() {
        return config.getBoolean("Receiver.RejectIllegalSubject", false);
    }

    private boolean bigSlaveLag() {
        return shouldWaitSlave() && waitSlaveSyncQueue.size() >= config.getInt("receive.queue.size", 50000);
    }

    private boolean shouldWaitSlave() {
        return config.getBoolean("wait.slave.wrote", false);
    }

    private void brokerReadOnly(ReceivedDelayMessage message) {
        QMon.delayBrokerReadOnlyMessageCountInc(message.getSubject());
        end(message, new ReceivedResult(message.getMessageId(), MessageProducerCode.BROKER_READ_ONLY, "BROKER_READ_ONLY"));
    }

    private void offer(ReceivedDelayMessage message, ReceivedResult result) {
        if (MessageProducerCode.SUCCESS != result.getCode()) {
            end(message, result);
            return;
        }

        if (!shouldWaitSlave()) {
            end(message, result);
            return;
        }

        waitSlaveSyncQueue.addLast(new ReceiveEntry(message, result));
    }

    private void overDelay(final ReceivedDelayMessage message) {
        LOGGER.warn("received delay message over delay,message:{}", message);
        QMon.overDelay(message.getSubject());
        adjustScheduleTime(message);
    }

    private void adjustScheduleTime(final ReceivedDelayMessage message) {
        long now = System.currentTimeMillis();
        message.adjustScheduleTime(now + TWO_YEAR_MILLIS);
    }

    private void error(ReceivedDelayMessage message, Throwable e) {
        LOGGER.error("delay broker receive message error,subject:{} ,id:{} ,msg:{}", message.getSubject(), message.getMessageId(), message, e);
        QMon.receiveFailedCuntInc(message.getSubject());
        end(message, new ReceivedResult(message.getMessageId(), MessageProducerCode.STORE_ERROR, "store error"));
    }

    private void end(ReceivedDelayMessage message, ReceivedResult result) {
        try {
            message.done(result);
        } catch (Throwable e) {
            LOGGER.error("send response failed id:{} ,msg:{}", message.getMessageId(), message);
        }
    }

    public static class SendResultPayloadHolder implements PayloadHolder {
        private final List<ReceivedResult> results;

        private final short version;

        public SendResultPayloadHolder(List<ReceivedResult> results, short version) {
            this.results = results;
            this.version = version;
        }

        @Override
        public void writeBody(ByteBuf out) {
            // VERSION_4 以下协议，返回所有消息信息
            if (version < RemotingHeader.VERSION_4) {
                writeBodyV3(out);
            } else {
                for (ReceivedResult result : results) {
                    if (MessageProducerCode.SUCCESS != result.getCode()) {
                        writeItem(result, out);
                    }
                }
            }
        }

        private void writeBodyV3(ByteBuf out) {
            for (ReceivedResult result : results) {
                writeItem(result, out);
            }
        }

        private void writeItem(ReceivedResult result, ByteBuf out) {
            int code = result.getCode();
            writeString(result.getMessageId(), out);
            out.writeInt(code);
            writeString(result.getRemark(), out);
        }

        private void writeString(String str, ByteBuf out) {
            byte[] bytes = CharsetUtils.toUTF8Bytes(str);
            if (bytes != null) {
                out.writeShort((short) bytes.length);
                out.writeBytes(bytes);
            } else {
                out.writeShort(0);
            }
        }
    }

    private static class ReceiveEntry {
        final ReceivedDelayMessage message;
        final ReceivedResult result;

        ReceiveEntry(ReceivedDelayMessage message, ReceivedResult result) {
            this.message = message;
            this.result = result;
        }
    }

}
