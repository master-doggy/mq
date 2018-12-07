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

package qunar.tc.qmq.consumer.pull;

import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qmq.Message;
import qunar.tc.qmq.PullConsumer;
import qunar.tc.qmq.broker.BrokerService;
import qunar.tc.qmq.common.StatusSource;
import qunar.tc.qmq.common.SwitchWaiter;

import java.util.List;
import java.util.concurrent.Future;

import static qunar.tc.qmq.common.StatusSource.CODE;

/**
 * @author yiqun.fan create on 17-10-19.
 */
abstract class AbstractPullConsumer implements PullConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPullConsumer.class);

    private static final long MIN_PULL_TIMEOUT_MILLIS = 1000;  // 最短拉取超时时间是1秒
    private static long MAX_PULL_TIMEOUT_MILLIS = Long.MAX_VALUE / 2;  // 最长拉取超时时间

    final SwitchWaiter onlineSwitcher = new SwitchWaiter(true);

    private final ConsumeParam consumeParam;
    private final ConsumeParam retryConsumeParam;
    final PlainPullEntry pullEntry;
    final PlainPullEntry retryPullEntry;

    AbstractPullConsumer(String subject, String group, boolean isBroadcast, String clientId, PullService pullService, AckService ackService, BrokerService brokerService) {
        this.consumeParam = new ConsumeParam(subject, group, isBroadcast, false, clientId);
        this.retryConsumeParam = new ConsumeParam(consumeParam.getRetrySubject(), group, isBroadcast, false, clientId);
        this.pullEntry = new PlainPullEntry(consumeParam, pullService, ackService, brokerService, new AlwaysPullStrategy());
        this.retryPullEntry = new PlainPullEntry(retryConsumeParam, pullService, ackService, brokerService, new WeightPullStrategy());
    }

    private static long checkAndGetTimeout(long timeout) {
        return timeout < 0 ? timeout : Math.min(Math.max(timeout, MIN_PULL_TIMEOUT_MILLIS), MAX_PULL_TIMEOUT_MILLIS);
    }

    @Override
    public void online() {
        online(CODE);
    }

    @Override
    public void offline() {
        offline(CODE);
    }

    public void online(StatusSource src) {
        onlineSwitcher.on(src);
        LOGGER.info("defaultpullconsumer online. subject={}, group={}", subject(), group());
    }

    public void offline(StatusSource src) {
        onlineSwitcher.off(src);
        LOGGER.info("defaultpullconsumer offline. subject={}, group={}", subject(), group());
    }

    @Override
    public String subject() {
        return consumeParam.getSubject();
    }

    @Override
    public String group() {
        return consumeParam.getGroup();
    }

    @Override
    public void setConsumeMostOnce(boolean consumeMostOnce) {
        consumeParam.setConsumeMostOnce(consumeMostOnce);
        retryConsumeParam.setConsumeMostOnce(consumeMostOnce);
    }

    @Override
    public boolean isConsumeMostOnce() {
        return consumeParam.isConsumeMostOnce();
    }

    @Override
    public List<Message> pull(int size) {
        return newFuture(size, MAX_PULL_TIMEOUT_MILLIS, false).get();
    }

    public List<Message> pull(int size, long timeoutMillis) {
        return newFuture(size, checkAndGetTimeout(timeoutMillis), false).get();
    }

    @Override
    public ListenableFuture<List<Message>> pullFuture(int size) {
        return newFuture(size, MAX_PULL_TIMEOUT_MILLIS, false);
    }

    @Override
    public Future<List<Message>> pullFuture(int size, long timeoutMillis) {
        return newFuture(size, checkAndGetTimeout(timeoutMillis), false);
    }

    @Override
    public Future<List<Message>> pullFuture(int size, long timeoutMillis, boolean isResetCreateTime) {
        return newFuture(size, checkAndGetTimeout(timeoutMillis), isResetCreateTime);
    }

    abstract PullMessageFuture newFuture(int size, long timeout, boolean isResetCreateTime);
}
