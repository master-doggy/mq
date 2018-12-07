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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qmq.delay.store.DelaySegmentValidator;
import qunar.tc.qmq.delay.store.appender.LogAppender;
import qunar.tc.qmq.delay.store.model.LogRecord;
import qunar.tc.qmq.delay.store.model.NopeRecordResult;
import qunar.tc.qmq.delay.store.model.RecordResult;
import qunar.tc.qmq.store.AppendMessageResult;
import qunar.tc.qmq.store.PutMessageStatus;

import java.io.File;
import java.util.concurrent.ConcurrentSkipListMap;

import static qunar.tc.qmq.delay.store.log.ScheduleOffsetResolver.resolveSegment;


/**
 * @author xufeng.deng dennisdxf@gmail.com
 * @since 2018-07-19 10:36
 */
public abstract class AbstractDelaySegmentContainer<T> implements SegmentContainer<RecordResult<T>, LogRecord> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDelaySegmentContainer.class);

    File logDir;

    private final LogAppender<T, LogRecord> appender;

    final ConcurrentSkipListMap<Integer, DelaySegment<T>> segments = new ConcurrentSkipListMap<>();

    AbstractDelaySegmentContainer(File logDir, DelaySegmentValidator validator, LogAppender<T, LogRecord> appender) {
        this.logDir = logDir;
        this.appender = appender;
        createAndValidateLogDir();
        loadLogs(validator);
    }

    protected abstract void loadLogs(DelaySegmentValidator validator);

    private void createAndValidateLogDir() {
        if (!logDir.exists()) {
            LOGGER.info("Log directory {} not found, try create it.", logDir.getAbsoluteFile());
            boolean created = logDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create log directory " + logDir.getAbsolutePath());
            }
        }

        if (!logDir.isDirectory() || !logDir.canRead() || !logDir.canWrite()) {
            throw new RuntimeException(logDir.getAbsolutePath() + " is not a readable log directory");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecordResult<T> append(LogRecord record) {
        long scheduleTime = record.getScheduleTime();
        DelaySegment<T> segment = locateSegment(scheduleTime);
        if (null == segment) {
            segment = allocNewSegment(scheduleTime);
        }

        if (null == segment) {
            return new NopeRecordResult(PutMessageStatus.CREATE_MAPPED_FILE_FAILED);
        }

        return retResult(segment.append(record, appender));
    }

    @Override
    public boolean clean(Long key) {
        if (segments.isEmpty()) return false;
        if (segments.lastKey() < key) return false;
        DelaySegment segment = segments.remove(Math.toIntExact(key));
        if (null == segment) {
            LOGGER.error("clean delay segment log failed,segment:{}", logDir, key);
            return false;
        }

        if (!segment.destroy()) {
            LOGGER.warn("remove delay segment failed.segment:{}", segment);
            return false;
        }

        LOGGER.info("remove delay segment success.segment:{}", segment);
        return true;
    }

    @Override
    public void flush() {
        for (DelaySegment<T> segment : segments.values()) {
            segment.flush();
        }
    }

    protected abstract RecordResult<T> retResult(AppendMessageResult<T> result);

    DelaySegment<T> locateSegment(long scheduleTime) {
        int baseOffset = resolveSegment(scheduleTime);
        return segments.get(baseOffset);
    }

    private DelaySegment<T> allocNewSegment(long offset) {
        int baseOffset = resolveSegment(offset);
        if (segments.containsKey(baseOffset)) {
            return segments.get(baseOffset);
        }
        return allocSegment(baseOffset);
    }

    int higherBaseOffset(int low) {
        Integer next = segments.higherKey(low);
        return next == null ? -1 : next;
    }

    protected abstract DelaySegment<T> allocSegment(int segmentBaseOffset);
}
