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

package qunar.tc.qmq.delay.store.model;

import qunar.tc.qmq.store.AppendMessageResult;
import qunar.tc.qmq.store.PutMessageStatus;

/**
 * @author xufeng.deng dennisdxf@gmail.com
 * @since 2018-07-19 15:01
 */
public class AppendScheduleLogRecordResult implements RecordResult<ScheduleSetSequence> {
    private PutMessageStatus status;

    private AppendMessageResult<ScheduleSetSequence> result;


    public AppendScheduleLogRecordResult(PutMessageStatus status, AppendMessageResult<ScheduleSetSequence> result) {
        this.status = status;
        this.result = result;
    }

    @Override
    public PutMessageStatus getStatus() {
        return status;
    }

    @Override
    public AppendMessageResult<ScheduleSetSequence> getResult() {
        return result;
    }

}
