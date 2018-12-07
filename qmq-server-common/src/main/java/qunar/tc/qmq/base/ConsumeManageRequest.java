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

package qunar.tc.qmq.base;

/**
 * @author yunfeng.yang
 * @since 2017/11/22
 */
public class ConsumeManageRequest {
    private int consumerFromWhere;
    private String subject;
    private String group;

    public int getConsumerFromWhere() {
        return consumerFromWhere;
    }

    public void setConsumerFromWhere(int consumerFromWhere) {
        this.consumerFromWhere = consumerFromWhere;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "ConsumeManageRequest{" +
                "consumerFromWhere=" + consumerFromWhere +
                ", subject='" + subject + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
