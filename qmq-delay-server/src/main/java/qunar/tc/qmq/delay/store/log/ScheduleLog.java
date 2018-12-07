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

package qunar.tc.qmq.delay.store.log;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qmq.common.Disposable;
import qunar.tc.qmq.delay.ScheduleIndex;
import qunar.tc.qmq.delay.base.LongHashSet;
import qunar.tc.qmq.delay.config.StoreConfiguration;
import qunar.tc.qmq.delay.store.DefaultDelaySegmentValidator;
import qunar.tc.qmq.delay.store.ScheduleLogValidatorSupport;
import qunar.tc.qmq.delay.store.appender.ScheduleSetAppender;
import qunar.tc.qmq.delay.store.model.*;
import qunar.tc.qmq.delay.store.visitor.LogVisitor;
import qunar.tc.qmq.delay.wheel.WheelLoadCursor;
import qunar.tc.qmq.protocol.producer.MessageProducerCode;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static qunar.tc.qmq.delay.store.ScheduleLogValidatorSupport.getSupport;


/**
 * @author xufeng.deng dennisdxf@gmail.com
 * @since 2018-08-02 17:47
 */
public class ScheduleLog implements Log<ByteBuf, LogRecord>, Disposable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleLog.class);

    private final ScheduleSet scheduleSet;
    private final AtomicBoolean open;
    private final StoreConfiguration config;

    public ScheduleLog(StoreConfiguration storeConfiguration) {
        final ScheduleSetSegmentContainer setContainer = new ScheduleSetSegmentContainer(
                storeConfiguration, new File(storeConfiguration.getScheduleLogStorePath())
                , new DefaultDelaySegmentValidator(), new ScheduleSetAppender(storeConfiguration.getSingleMessageLimitSize()));

        this.config = storeConfiguration;
        this.scheduleSet = new ScheduleSet(setContainer);
        this.open = new AtomicBoolean(true);
        reValidate(storeConfiguration.getSingleMessageLimitSize());
    }

    private void reValidate(int singleMessageLimitSize) {
        ScheduleLogValidatorSupport support = ScheduleLogValidatorSupport.getSupport(config);
        Map<Integer, Long> offsets = support.loadScheduleOffsetCheckpoint();
        scheduleSet.reValidate(offsets, singleMessageLimitSize);
    }

    @Override
    public AppendLogResult<ByteBuf> append(LogRecord record) {
        if (!open.get()) {
            return new AppendLogResult<>(MessageProducerCode.STORE_ERROR, "schedule log closed");
        }
        AppendLogResult<RecordResult<ScheduleSetSequence>> result = scheduleSet.append(record);
        int code = result.getCode();
        if (MessageProducerCode.SUCCESS != code) {
            LOGGER.error("appendMessageLog schedule set error,log:{} {},code:{}", record.getSubject(), record.getMessageId(), code);
            return new AppendLogResult<>(MessageProducerCode.STORE_ERROR, "appendScheduleSetError");
        }

        RecordResult<ScheduleSetSequence> recordResult = result.getAdditional();
        ByteBuf index = buildIndexRecord(record.getScheduleTime(), recordResult.getResult().getWroteOffset(),
                recordResult.getResult().getWroteBytes(), recordResult.getResult().getAdditional().getSequence());

        return new AppendLogResult<>(MessageProducerCode.SUCCESS, "", index);
    }

    private ByteBuf buildIndexRecord(long scheduleTime, long offset, int size, long sequence) {
        return ScheduleIndex.buildIndex(scheduleTime, offset, size, sequence);
    }

    @Override
    public boolean clean(Long key) {
        return scheduleSet.clean(key);
    }

    @Override
    public void flush() {
        if (open.get()) {
            scheduleSet.flush();
        }
    }

    public List<ScheduleSetRecord> recoverLogRecord(List<ByteBuf> pureRecords) {
        try {
            List<ScheduleSetRecord> records = Lists.newArrayListWithCapacity(pureRecords.size());
            for (ByteBuf record : pureRecords) {
                ScheduleSetRecord logRecord = scheduleSet.recoverRecord(record);
                if (logRecord == null) {
                    LOGGER.error("schedule log recover null record");
                    continue;
                }

                records.add(logRecord);
            }

            return records;
        } finally {
            ScheduleIndex.release(pureRecords);
        }
    }

    public void clean() {
        scheduleSet.clean();
    }

    public WheelLoadCursor.Cursor loadUnDispatch(ScheduleSetSegment segment, final LongHashSet dispatchedSet, final Consumer<ByteBuf> func) {
        LogVisitor<ByteBuf> visitor = segment.newVisitor(0, config.getSingleMessageLimitSize());
        try {
            long offset = 0;
            while (true) {
                Optional<ByteBuf> recordOptional = visitor.nextRecord();
                if (!recordOptional.isPresent()) break;
                ByteBuf index = recordOptional.get();
                long sequence = ScheduleIndex.sequence(index);
                offset = ScheduleIndex.offset(index) + ScheduleIndex.size(index);
                if (!dispatchedSet.contains(sequence)) {
                    func.accept(index);
                } else {
                    ScheduleIndex.release(index);
                }
            }
            return new WheelLoadCursor.Cursor(segment.getSegmentBaseOffset(), offset);
        } finally {
            visitor.close();
            LOGGER.info("schedule log recover {} which is need to continue to dispatch.", segment.getSegmentBaseOffset());
        }
    }

    public ScheduleSetSegment loadSegment(int segmentBaseOffset) {
        return scheduleSet.loadSegment(segmentBaseOffset);
    }

    @Override
    public void destroy() {
        open.set(false);
        getSupport(config).saveScheduleOffsetCheckpoint(checkOffsets());
    }

    private Map<Integer, Long> checkOffsets() {
        return scheduleSet.countSegments();
    }

    public int higherBaseOffset(int low) {
        return scheduleSet.higherBaseOffset(low);
    }
}
