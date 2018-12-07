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

package qunar.tc.qmq.protocol.producer;

/**
 * @author zhenyu.nie created on 2017 2017/7/6 16:04
 */
public class MessageProducerCode {

    public static final int SUCCESS = 0;
    public static final int BROKER_BUSY = 1;
    public static final int MESSAGE_DUPLICATE = 2;
    public static final int SUBJECT_NOT_ASSIGNED = 3;
    public static final int BROKER_READ_ONLY = 4;
    public static final int BLOCK = 5;
    public static final int STORE_ERROR = 6;

}
