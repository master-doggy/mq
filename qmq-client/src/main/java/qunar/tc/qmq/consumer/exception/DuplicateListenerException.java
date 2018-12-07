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

package qunar.tc.qmq.consumer.exception;

/**
 * User: zhaohuiyu
 * Date: 10/15/13
 * Time: 1:53 PM
 */
public class DuplicateListenerException extends RuntimeException {
    private static final long serialVersionUID = 1377475808662809865L;
    private String key;

    public DuplicateListenerException(String key) {
        super(key);
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
