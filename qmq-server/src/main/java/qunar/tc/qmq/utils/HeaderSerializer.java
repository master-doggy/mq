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

package qunar.tc.qmq.utils;

import qunar.tc.qmq.protocol.RemotingHeader;

import java.nio.ByteBuffer;

import static qunar.tc.qmq.protocol.RemotingHeader.*;

public class HeaderSerializer {
    public static ByteBuffer serialize(RemotingHeader header, int payloadSize, int additional) {
        short headerLength = MIN_HEADER_SIZE;
        if (header.getVersion() >= RemotingHeader.VERSION_3) {
            headerLength += REQUEST_CODE_LEN;
        }

        int bufferLength = TOTAL_SIZE_LEN + HEADER_SIZE_LEN + headerLength + additional;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        // total len
        int total = HEADER_SIZE_LEN + headerLength + payloadSize;
        buffer.putInt(total);
        // header len
        buffer.putShort(headerLength);
        // magic code
        buffer.putInt(header.getMagicCode());
        // code
        buffer.putShort(header.getCode());
        // version
        buffer.putShort(header.getVersion());
        // opaque
        buffer.putInt(header.getOpaque());
        // flag
        buffer.putInt(header.getFlag());
        if (header.getVersion() >= RemotingHeader.VERSION_3) {
            buffer.putShort(header.getRequestCode());
        }
        return buffer;
    }
}
