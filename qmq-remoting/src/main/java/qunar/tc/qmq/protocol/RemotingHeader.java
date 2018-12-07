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

package qunar.tc.qmq.protocol;

/**
 * @author yiqun.fan create on 17-7-4.
 */
public class RemotingHeader {
    public static final int DEFAULT_MAGIC_CODE = 0xdec1_0ade;

    public static final short VERSION_1 = 1;
    public static final short VERSION_2 = 2;
    public static final short VERSION_3 = 3;
    public static final short VERSION_4 = 4;
    /**
     * (deprecated) compute crc only on message attr
     */
    public static final short VERSION_5 = 5;
    /**
     * add crc while sending message to broker
     */
    public static final short VERSION_6 = 6;
    /**
     * add schedule time in message header for delay message
     */
    public static final short VERSION_7 = 7;

    /**
     * add tags field for message header
     */
    public static final short VERSION_8 = 8;

    public static final short MIN_HEADER_SIZE = 16;  // magic code(4) + code(2) + version(2) + opaque(4) + flag(4)
    public static final short HEADER_SIZE_LEN = 2;
    public static final short TOTAL_SIZE_LEN = 4;

    public static final short LENGTH_FIELD = TOTAL_SIZE_LEN + HEADER_SIZE_LEN;

    public static final short REQUEST_CODE_LEN = 2;

    private int magicCode = DEFAULT_MAGIC_CODE;
    private short code;
    private short version = VERSION_8;
    private int opaque;
    private int flag;
    private short requestCode = CommandCode.PLACEHOLDER;

    public int getMagicCode() {
        return magicCode;
    }

    public void setMagicCode(int magicCode) {
        this.magicCode = magicCode;
    }

    public short getCode() {
        return code;
    }

    public void setCode(short code) {
        this.code = code;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public short getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(short requestCode) {
        this.requestCode = requestCode;
    }

    @Override
    public String toString() {
        return "RemotingHeader{" +
                "magicCode=" + magicCode +
                ", code=" + code +
                ", version=" + version +
                ", opaque=" + opaque +
                ", flag=" + flag +
                ", requestCode=" + requestCode +
                '}';
    }
}
