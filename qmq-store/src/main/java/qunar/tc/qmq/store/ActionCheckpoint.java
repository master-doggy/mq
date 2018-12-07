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

package qunar.tc.qmq.store;

import com.google.common.collect.Table;

/**
 * @author keli.wang
 * @since 2018/9/10
 */
public class ActionCheckpoint {
    // mark this object is read from old version snapshot file
    // TODO(keli.wang): delete this after all broker group is migrate to new snapshot format
    private final boolean fromOldVersion;
    private final Table<String, String, ConsumerGroupProgress> progresses;
    private long offset;

    public ActionCheckpoint(long offset, Table<String, String, ConsumerGroupProgress> progresses) {
        this(false, offset, progresses);
    }

    public ActionCheckpoint(boolean fromOldVersion, long offset, Table<String, String, ConsumerGroupProgress> progresses) {
        this.fromOldVersion = fromOldVersion;
        this.offset = offset;
        this.progresses = progresses;
    }

    public boolean isFromOldVersion() {
        return fromOldVersion;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Table<String, String, ConsumerGroupProgress> getProgresses() {
        return progresses;
    }
}
