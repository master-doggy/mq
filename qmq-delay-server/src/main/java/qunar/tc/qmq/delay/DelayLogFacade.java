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

package qunar.tc.qmq.delay;

import io.netty.buffer.ByteBuf;
import qunar.tc.qmq.delay.base.LongHashSet;
import qunar.tc.qmq.delay.base.ReceivedDelayMessage;
import qunar.tc.qmq.delay.base.ReceivedResult;
import qunar.tc.qmq.delay.store.log.DispatchLogSegment;
import qunar.tc.qmq.delay.store.log.ScheduleSetSegment;
import qunar.tc.qmq.delay.store.model.AppendLogResult;
import qunar.tc.qmq.delay.store.model.LogRecord;
import qunar.tc.qmq.delay.store.model.ScheduleSetRecord;
import qunar.tc.qmq.delay.store.visitor.LogVisitor;
import qunar.tc.qmq.delay.wheel.WheelLoadCursor;
import qunar.tc.qmq.store.SegmentBuffer;
import qunar.tc.qmq.sync.DelaySyncRequest;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author xufeng.deng dennisdxf@gmail.com
 * @since 2018-07-17 16:57
 */
public interface DelayLogFacade {
    void start();

    void shutdown();

    ReceivedResult appendMessageLog(ReceivedDelayMessage message);

    long getMessageLogMinOffset();

    SegmentBuffer getMessageLogs(long startSyncOffset);

    SegmentBuffer getDispatchLogs(int segmentBaseOffset, long dispatchLogOffset);

    long getMessageLogMaxOffset();

    long getDispatchLogMaxOffset(int dispatchSegmentBaseOffset);

    DelaySyncRequest.DispatchLogSyncRequest getDispatchLogSyncMaxRequest();

    boolean appendMessageLogData(long startOffset, ByteBuffer buffer);

    boolean appendDispatchLogData(long startOffset, int baseOffset, ByteBuffer body);

    List<ScheduleSetRecord> recoverLogRecord(List<ByteBuf> pureRecords);

    void appendDispatchLog(LogRecord record);

    DispatchLogSegment latestDispatchSegment();

    DispatchLogSegment lowerDispatchSegment(int latestOffset);

    ScheduleSetSegment loadScheduleLogSegment(int segmentBaseOffset);

    WheelLoadCursor.Cursor loadUnDispatch(ScheduleSetSegment setSegment, LongHashSet dispatchedSet, Consumer<ByteBuf> refresh);

    int higherScheduleBaseOffset(int index);

    LogVisitor<LogRecord> newMessageLogVisitor(long start);

    AppendLogResult<ByteBuf> appendScheduleLog(LogRecord event);

    long initialMessageIterateFrom();

    void updateIterateOffset(long checkpoint);

    void blockUntilReplayDone();

    int higherDispatchLogBaseOffset(int segmentBaseOffset);
}
