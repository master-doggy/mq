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

/**
 * @author keli.wang
 * @since 2017/8/23
 */
public class MessageLogMeta {
    private final String subject;
    private final long sequence;
    private final long wroteOffset;
    private final int wroteBytes;
    private final short headerSize;
    private final long baseOffset;

    public MessageLogMeta(String subject, long sequence, long wroteOffset, int wroteBytes, short headerSize, long baseOffset) {
        this.subject = subject;
        this.sequence = sequence;
        this.wroteOffset = wroteOffset;
        this.wroteBytes = wroteBytes;
        this.headerSize = headerSize;
        this.baseOffset = baseOffset;
    }

    public String getSubject() {
        return subject;
    }

    public long getSequence() {
        return sequence;
    }


    public long getWroteOffset() {
        return wroteOffset;
    }

    public int getWroteBytes() {
        return wroteBytes;
    }

    public short getHeaderSize() {
        return headerSize;
    }

    public long getBaseOffset() {
        return baseOffset;
    }

    @Override
    public String toString() {
        return "MessageLogMeta{" +
                "subject='" + subject + '\'' +
                ", sequence=" + sequence +
                ", wroteOffset=" + wroteOffset +
                ", wroteBytes=" + wroteBytes +
                ", headerSize=" + headerSize +
                ", baseOffset=" + baseOffset +
                '}';
    }
}
