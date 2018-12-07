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
package qunar.tc.qmq.consumer.handler;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import qunar.tc.qmq.ListenerHolder;
import qunar.tc.qmq.MessageListener;
import qunar.tc.qmq.SubscribeParam;
import qunar.tc.qmq.consumer.register.ConsumerRegister;
import qunar.tc.qmq.consumer.register.RegistParam;

import java.util.concurrent.Executor;

import static qunar.tc.qmq.common.StatusSource.CODE;

/**
 * @author miao.yang susing@gmail.com
 * @date 2012-12-28
 */
public class MessageDistributor {
    private final ConsumerRegister register;

    private String clientId;

    public MessageDistributor(ConsumerRegister register) {
        this.register = register;
    }

    public ListenerHolder addListener(final String subjectPrefix, final String consumerGroup, MessageListener listener, Executor executor, SubscribeParam subscribeParam) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(consumerGroup));

        final RegistParam registParam = new RegistParam(executor, listener, subscribeParam, clientId);
        registParam.setBroadcast(subscribeParam.isBroadcast());
        register.regist(subjectPrefix, consumerGroup, registParam);
        return new ListenerHolder() {

            @Override
            public void stopListen() {
                register.unregist(subjectPrefix, consumerGroup);
            }

            @Override
            public void resumeListen() {
                registParam.setActionSrc(CODE);
                register.regist(subjectPrefix, consumerGroup, registParam);
            }
        };
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
