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
package qunar.tc.qmq.consumer;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import qunar.tc.qmq.*;
import qunar.tc.qmq.common.ClientIdProvider;
import qunar.tc.qmq.common.ClientIdProviderFactory;
import qunar.tc.qmq.config.NettyClientConfigManager;
import qunar.tc.qmq.consumer.handler.MessageDistributor;
import qunar.tc.qmq.consumer.pull.PullConsumerFactory;
import qunar.tc.qmq.consumer.pull.PullRegister;
import qunar.tc.qmq.netty.client.NettyClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;

/**
 * @author miao.yang susing@gmail.com
 * @date 2012-12-28
 */
public class MessageConsumerProvider implements MessageConsumer {

    private static final int MAX_CONSUMER_GROUP_LEN = 50;
    private static final int MAX_PREFIX_LEN = 100;

    private MessageDistributor distributor;

    private final PullConsumerFactory pullConsumerFactory;

    private volatile boolean inited = false;

    private ClientIdProvider clientIdProvider;

    private final PullRegister pullRegister;
    private String appCode;
    private String metaServer;
    private int destroyWaitInSeconds;

    public MessageConsumerProvider() {
        this.clientIdProvider = ClientIdProviderFactory.createDefault();
        this.pullRegister = new PullRegister();
        this.pullConsumerFactory = new PullConsumerFactory(this.pullRegister);
    }

    @PostConstruct
    public void init() {
        Preconditions.checkNotNull(appCode, "appCode是应用的唯一标识");
        Preconditions.checkNotNull(metaServer, "metaServer是meta server的地址");

        if (inited) return;

        synchronized (this) {
            if (inited) return;

            NettyClient.getClient().start(NettyClientConfigManager.get().getDefaultClientConfig());

            String clientId = this.clientIdProvider.get();
            this.pullRegister.setDestroyWaitInSeconds(destroyWaitInSeconds);
            this.pullRegister.setMetaServer(metaServer);
            this.pullRegister.setClientId(clientId);
            this.pullRegister.init();

            distributor = new MessageDistributor(pullRegister);
            distributor.setClientId(clientId);

            pullRegister.setAutoOnline(true);
            inited = true;
        }
    }

    @Override
    public ListenerHolder addListener(String subject, String consumerGroup, MessageListener listener, Executor executor) {
        return addListener(subject, consumerGroup, listener, executor, SubscribeParam.DEFAULT);
    }

    @Override
    public ListenerHolder addListener(String subject, String consumerGroup, MessageListener listener, Executor executor, SubscribeParam subscribeParam) {
        init();
        Preconditions.checkArgument(subject != null && subject.length() <= MAX_PREFIX_LEN, "subjectPrefix长度不允许超过" + MAX_PREFIX_LEN + "个字符");
        Preconditions.checkArgument(consumerGroup == null || consumerGroup.length() <= MAX_CONSUMER_GROUP_LEN, "consumerGroup长度不允许超过" + MAX_CONSUMER_GROUP_LEN + "个字符");

        Preconditions.checkArgument(!subject.contains("${"), "请确保subject已经正确解析: " + subject);
        Preconditions.checkArgument(consumerGroup == null || !consumerGroup.contains("${"), "请确保consumerGroup已经正确解析: " + consumerGroup);

        if (Strings.isNullOrEmpty(consumerGroup)) {
            subscribeParam.setBroadcast(true);
        }

        if (subscribeParam.isBroadcast()) {
            consumerGroup = clientIdProvider.get();
        }

        Preconditions.checkNotNull(executor, "消费逻辑将在该线程池里执行");
        Preconditions.checkNotNull(subscribeParam, "订阅时候的参数需要指定，如果使用默认参数的话请使用无此参数的重载");

        return distributor.addListener(subject, consumerGroup, listener, executor, subscribeParam);
    }

    @Override
    public PullConsumer getOrCreatePullConsumer(String subject, String group, boolean isBroadcast) {
        init();

        Preconditions.checkArgument(!Strings.isNullOrEmpty(subject), "subject不能是nullOrEmpty");
        if (!isBroadcast) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "非广播订阅时，group不能是nullOrEmpty");
        } else {
            group = clientIdProvider.get();
        }
        return pullConsumerFactory.getOrCreateDefault(subject, group, isBroadcast);
    }

    public void setClientIdProvider(ClientIdProvider clientIdProvider) {
        this.clientIdProvider = clientIdProvider;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public void setMetaServer(String metaServer) {
        this.metaServer = metaServer;
    }

    public void setDestroyWaitInSeconds(int destroyWaitInSeconds) {
        this.destroyWaitInSeconds = destroyWaitInSeconds;
    }

    @PreDestroy
    public void destroy() {
        pullRegister.destroy();
    }
}
