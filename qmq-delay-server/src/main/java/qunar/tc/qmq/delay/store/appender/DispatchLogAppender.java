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

package qunar.tc.qmq.delay.store.appender;

import qunar.tc.qmq.delay.store.model.AppendRecordResult;
import qunar.tc.qmq.delay.store.model.LogRecord;
import qunar.tc.qmq.store.AppendMessageStatus;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xufeng.deng dennisdxf@gmail.com
 * @since 2018-07-19 16:43
 */
public class DispatchLogAppender implements LogAppender<Boolean, LogRecord> {
    private final ByteBuffer workingBuffer = ByteBuffer.allocate(Long.BYTES);
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public AppendRecordResult<Boolean> appendLog(LogRecord log) {
        workingBuffer.clear();
        workingBuffer.putLong(log.getSequence());
        workingBuffer.flip();

        return new AppendRecordResult<>(AppendMessageStatus.SUCCESS, 0, Long.BYTES, workingBuffer,true);
    }

    @Override
    public void lockAppender() {
        lock.lock();
    }

    @Override
    public void unlockAppender() {
        lock.unlock();
    }
}
