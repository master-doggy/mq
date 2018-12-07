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
package qunar.tc.qmq.producer;

import com.google.common.base.Preconditions;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import qunar.tc.qmq.Message;
import qunar.tc.qmq.MessageProducer;
import qunar.tc.qmq.MessageSendStateListener;
import qunar.tc.qmq.base.BaseMessage;
import qunar.tc.qmq.common.ClientIdProvider;
import qunar.tc.qmq.common.ClientIdProviderFactory;
import qunar.tc.qmq.producer.idgenerator.IdGenerator;
import qunar.tc.qmq.producer.idgenerator.TimestampAndHostIdGenerator;
import qunar.tc.qmq.producer.sender.NettyRouterManager;
import qunar.tc.qmq.tracing.TraceUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author miao.yang susing@gmail.com
 * @date 2013-1-5
 */
public class MessageProducerProvider implements MessageProducer {
    private static final ConfigCenter configs = ConfigCenter.getInstance();

    private final IdGenerator idGenerator;

    private final AtomicBoolean STARTED = new AtomicBoolean(false);

    private final NettyRouterManager routerManager;

    private ClientIdProvider clientIdProvider;

    private final Tracer tracer;

    private String appCode;
    private String metaServer;

    /**
     * 自动路由机房
     */
    public MessageProducerProvider() {
        this.idGenerator = new TimestampAndHostIdGenerator();
        this.clientIdProvider = ClientIdProviderFactory.createDefault();
        this.routerManager = new NettyRouterManager();
        this.tracer = GlobalTracer.get();
    }

    @PostConstruct
    public void init() {
        Preconditions.checkNotNull(appCode, "appCode唯一标识一个应用");
        Preconditions.checkNotNull(metaServer, "metaServer的http地址");

        this.routerManager.setMetaServer(this.metaServer);

        if (STARTED.compareAndSet(false, true)) {
            routerManager.init(clientIdProvider.get());
        }
    }

    @Override
    public Message generateMessage(String subject) {
        BaseMessage msg = new BaseMessage(idGenerator.getNext(), subject);
        msg.setExpiredDelay(configs.getMinExpiredTime(), TimeUnit.MINUTES);
        msg.setProperty(BaseMessage.keys.qmq_appCode, appCode);
        return msg;
    }

    @Override
    public void sendMessage(Message message) {
        sendMessage(message, null);
    }

    @Override
    public void sendMessage(Message message, MessageSendStateListener listener) {
        if (!STARTED.get()) {
            throw new RuntimeException("MessageProducerProvider未初始化，如果使用非Spring的方式请确认init()是否调用");
        }
        try (Scope ignored = tracer.buildSpan("Qmq.Produce.Send")
                .withTag("appCode", appCode)
                .withTag("subject", message.getSubject())
                .withTag("messageId", message.getMessageId())
                .startActive(true)) {
            ProduceMessageImpl pm = initProduceMessage(message, listener);
            pm.send();
        }
    }

    private ProduceMessageImpl initProduceMessage(Message message, MessageSendStateListener listener) {
        BaseMessage base = (BaseMessage) message;
        routerManager.validateMessage(message);
        ProduceMessageImpl pm = new ProduceMessageImpl(base, routerManager.getSender());
        pm.setSendTryCount(configs.getSendTryCount());
        pm.setSendStateListener(listener);
        pm.setSyncSend(configs.isSyncSend());

        String value = routerManager.registryOf(message);
        TraceUtil.setTag("registry", value, tracer);
        return pm;
    }

    @PreDestroy
    public void destroy() {
        routerManager.destroy();
    }

    /**
     * 内存发送队列最大值，默认值 @see QUEUE_MEM_SIZE
     *
     * @param maxQueueSize 内存队列大小
     */
    public void setMaxQueueSize(int maxQueueSize) {
        configs.setMaxQueueSize(maxQueueSize);
    }

    /**
     * 发送线程数 @see SEND_THREADS
     *
     * @param sendThreads
     */
    public void setSendThreads(int sendThreads) {
        configs.setSendThreads(sendThreads);
    }

    /**
     * 批量发送，每批量大小 @see SEND_BATCH
     *
     * @param sendBatch
     */
    public void setSendBatch(int sendBatch) {
        configs.setSendBatch(sendBatch);
    }

    /**
     * 发送失败重试次数
     *
     * @param sendTryCount @see SEND_TRY_COUNT
     */
    public void setSendTryCount(int sendTryCount) {
        configs.setSendTryCount(sendTryCount);
    }

    public void setClientIdProvider(ClientIdProvider clientIdProvider) {
        this.clientIdProvider = clientIdProvider;
    }

    /**
     * 为了方便维护应用与消息主题之间的关系，每个应用提供一个唯一的标识
     *
     * @param appCode
     */
    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    /**
     * 用于发现meta server集群的地址
     *
     * @param metaServer
     */
    public void setMetaServer(String metaServer) {
        this.metaServer = metaServer;
    }
}