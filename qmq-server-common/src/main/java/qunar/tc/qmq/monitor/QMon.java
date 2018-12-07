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

package qunar.tc.qmq.monitor;

import com.google.common.base.Supplier;
import qunar.tc.qmq.metrics.Metrics;

import java.util.concurrent.TimeUnit;

import static qunar.tc.qmq.metrics.MetricsConstants.SUBJECT_ARRAY;
import static qunar.tc.qmq.metrics.MetricsConstants.SUBJECT_GROUP_ARRAY;

/**
 * @author yunfeng.yang
 * @since 2017/7/10
 */
public final class QMon {
    private static final String[] NONE = {};
    private static final String[] CONSUMER_ID = {"consumerId"};
    private static final String[] REQUEST_CODE = {"requestCode"};

    private static void subjectCountInc(String name, String subject) {
        countInc(name, SUBJECT_ARRAY, new String[]{subject});
    }

    private static void subjectAndGroupCountInc(String name, String subjectPrefix, String consumerGroup) {
        countInc(name, SUBJECT_GROUP_ARRAY, new String[]{subjectPrefix, consumerGroup});
    }

    private static void subjectAndGroupCountInc(String name, String[] values, long num) {
        countInc(name, SUBJECT_GROUP_ARRAY, values, num);
    }

    private static void countInc(String name, String[] tags, String[] values) {
        Metrics.counter(name, tags, values).inc();
    }

    private static void countDec(String name, String[] tags, String[] values) {
        Metrics.counter(name, tags, values).dec();
    }

    private static void countInc(String name, String[] tags, String[] values, long num) {
        Metrics.counter(name, tags, values).inc(num);
    }

    public static void produceTime(String subject, long time) {
        Metrics.timer("produceTime", SUBJECT_ARRAY, new String[]{subject}).update(time, TimeUnit.MILLISECONDS);
    }

    public static void receivedMessagesCountInc(String subject) {
        subjectCountInc("receivedMessagesCount", subject);
        final String[] values = {subject};
        Metrics.meter("receivedMessagesEx", SUBJECT_ARRAY, values).mark();
    }

    public static void receivedIllegalSubjectMessagesCountInc(String subject) {
        subjectCountInc("receivedIllegalSubjectMessagesCount", subject);
    }

    public static void pulledMessagesCountInc(String subject, String group, int messageNum) {
        final String[] values = new String[]{subject, group};
        subjectAndGroupCountInc("pulledMessagesCount", values, messageNum);
        Metrics.meter("pulledMessagesEx", SUBJECT_GROUP_ARRAY, values).mark(messageNum);
    }

    public static void pulledNoMessagesCountInc(String subject, String group) {
        subjectAndGroupCountInc("pulledNoMessagesCount", subject, group);
    }

    public static void pulledMessageBytesCountInc(String subject, String group, int bytes) {
        subjectAndGroupCountInc("pulledMessageBytesCount", new String[]{subject, group}, bytes);
    }

    public static void storeMessageErrorCountInc(String subject) {
        subjectCountInc("storeMessageErrorCount", subject);
    }

    public static void pullQueueTime(String subject, String group, long start) {
        Metrics.timer("pullQueueTime", SUBJECT_GROUP_ARRAY, new String[]{subject, group}).update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
    }

    public static void suspendRequestCountInc(String subject, String group) {
        subjectAndGroupCountInc("suspendRequestCount", subject, group);
    }

    public static void resumeActorCountInc(String subject, String group) {
        subjectAndGroupCountInc("resumeActorCount", subject, group);
    }

    public static void pullTimeOutCountInc(String subject, String group) {
        subjectAndGroupCountInc("pullTimeOutCount", subject, group);
    }

    public static void pullExpiredCountInc(String subject, String group) {
        subjectAndGroupCountInc("pullExpiredCount", subject, group);
    }

    public static void getMessageErrorCountInc(String subject, String group) {
        subjectAndGroupCountInc("getMessageErrorCount", subject, group);
    }

    public static void getMessageOverflowCountInc(String subject, String group) {
        subjectAndGroupCountInc("getMessageOverflowCount", subject, group);
    }

    public static void consumerAckCountInc(String subject, String group, int size) {
        subjectAndGroupCountInc("consumerAckCount", new String[]{subject, group}, size);
    }

    public static void consumerLostAckCountInc(String subject, String group, int lostAckCount) {
        subjectAndGroupCountInc("consumerLostAckCount", new String[]{subject, group}, lostAckCount);
    }

    public static void consumerDuplicateAckCountInc(String subject, String group, int duplicateAckCount) {
        subjectAndGroupCountInc("consumerDuplicateAckCount", new String[]{subject, group}, duplicateAckCount);
    }

    public static void consumerAckTimeoutErrorCountInc(String consumerId, int num) {
        countInc("consumerAckTimeoutErrorCountInc", CONSUMER_ID, new String[]{consumerId}, num);
    }

    public static void putMessageTime(String subject, long time) {
        Metrics.timer("putMessageTime", SUBJECT_ARRAY, new String[]{subject}).update(time, TimeUnit.MILLISECONDS);
    }

    public static void processTime(String subject, long time) {
        Metrics.timer("processTime", SUBJECT_ARRAY, new String[]{subject}).update(time, TimeUnit.MILLISECONDS);
    }

    public static void rejectReceivedMessageCountInc(String subject) {
        subjectCountInc("rejectReceivedMessageCount", subject);
    }

    public static void brokerReadOnlyMessageCountInc(String subject) {
        subjectCountInc("brokerReadOnlyMessageCount", subject);
    }

    public static void receivedFailedCountInc(String subject) {
        subjectCountInc("receivedFailedCount", subject);
    }

    public static void receiveExpiredMessagesCountInc(String subject) {
        subjectCountInc("receiveExpiredMessagesCount", subject);
    }

    public static void ackProcessTime(String subject, String group, long elapsed) {
        Metrics.timer("ackProcessTime", SUBJECT_GROUP_ARRAY, new String[]{subject, group}).update(elapsed, TimeUnit.MILLISECONDS);
    }

    public static void pullProcessTime(String subject, String group, long elapsed) {
        Metrics.timer("pullProcessTime", SUBJECT_GROUP_ARRAY, new String[]{subject, group}).update(elapsed, TimeUnit.MILLISECONDS);
    }

    public static void putActionFailedCountInc(String subject, String group) {
        subjectAndGroupCountInc("putActionFailedCount", subject, group);
    }

    public static void findLostMessageCountInc(String subject, String group, int messageNum) {
        subjectAndGroupCountInc("findLostMessageCount", new String[]{subject, group}, messageNum);
    }

    public static void findLostMessageEmptyCountInc(String subject, String group) {
        subjectAndGroupCountInc("findLostMessageEmptyCount", subject, group);
    }

    public static void pullRequestCountInc(String subject, String group) {
        subjectAndGroupCountInc("pullRequestCount", subject, group);
        Metrics.meter("pullRequestEx", SUBJECT_GROUP_ARRAY, new String[]{subject, group}).mark();
    }

    public static void ackRequestCountInc(String subject, String group) {
        subjectAndGroupCountInc("ackRequestCount", subject, group);
        Metrics.meter("ackRequestEx", SUBJECT_GROUP_ARRAY, new String[]{subject, group}).mark();
    }

    public static void pullParamErrorCountInc(String subject, String group) {
        subjectAndGroupCountInc("pullParamErrorCount", subject, group);
    }

    public static void nonPositiveRequestNumCountInc(String subject, String group) {
        subjectAndGroupCountInc("nonPositiveRequestNumCount", subject, group);
    }

    public static void putAckActionsErrorCountInc(String subject, String group) {
        subjectAndGroupCountInc("putAckActionsErrorCount", subject, group);
    }

    public static void findMessagesErrorCountInc(String subject, String group) {
        subjectAndGroupCountInc("findMessagesErrorCount", subject, group);
    }

    public static void putNeedRetryMessagesCountInc(String subject, String group, int size) {
        subjectAndGroupCountInc("putNeedRetryMessagesCount", new String[]{subject, group}, size);
    }

    public static void consumerLogOffsetRangeError(String subject, String group) {
        subjectAndGroupCountInc("consumerLogOffsetRangeErrorCount", subject, group);
    }

    public static void consumerErrorCount(String subject, String group) {
        subjectAndGroupCountInc("consumerErrorCount", subject, group);
    }

    public static void deadLetterQueueCount(String subject, String group) {
        subjectAndGroupCountInc("deadLetterQueueCount", subject, group);
    }

    public static void expiredMessagesCountInc(String subject, String group, long num) {
        subjectAndGroupCountInc("ExpiredMessages", new String[]{subject, group}, num);
    }

    public static void readMessageReturnNullCountInc(String subject) {
        subjectCountInc("ReadMessageReturnNull", subject);
    }

    public static void replayMessageLogFailedCountInc() {
        countInc("ReplayMessageLogFailed", NONE, NONE);
    }

    public static void replayActionLogFailedCountInc() {
        countInc("ReplayActionLogFailed", NONE, NONE);
    }

    public static void logSegmentTotalRefCountInc() {
        countInc("LogSegment.TotalRefCount", NONE, NONE);
    }

    public static void logSegmentTotalRefCountDec() {
        countDec("LogSegment.TotalRefCount", NONE, NONE);
    }

    public static void maybeLostMessagesCountInc(String subject, String group, long num) {
        subjectAndGroupCountInc("maybeLostMessages", new String[]{subject, group}, num);
    }

    public static void retryTaskExecuteCountInc(String subject, String group) {
        subjectAndGroupCountInc("ConsumerStatusChecker.RetryTask.Execute", subject, group);
    }

    public static void offlineTaskExecuteCountInc(String subject, String group) {
        subjectAndGroupCountInc("ConsumerStatusChecker.OfflineTask.Execute", subject, group);
    }

    public static void brokerReceivedInvalidMessageCountInc() {
        countInc("Broker.ReceivedInvalidMessage", NONE, NONE);
    }

    public static void findNewExistMessageTime(String subject, String group, long elapsedMillis) {
        Metrics.timer("findNewExistMessagesTime", SUBJECT_GROUP_ARRAY, new String[]{subject, group}).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void findLostMessagesTime(String subject, String group, long elapsedMillis) {
        Metrics.timer("findLostMessagesTime", SUBJECT_GROUP_ARRAY, new String[]{subject, group}).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void readPullResultAsBytesElapsed(String subject, String group, long elapsedMillis) {
        Metrics.timer("Broker.PullResult.ReadAsBytesElapsed", SUBJECT_GROUP_ARRAY, new String[]{subject, group}).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void flushPullLogCountInc() {
        countInc("Store.PullLog.FlushCount", NONE, NONE);
    }

    public static void flushConsumerLogCountInc(String subject) {
        subjectCountInc("Store.ConsumerLog.FlushCount", subject);
    }

    public static void flushPullLogTimer(long elapsedMillis) {
        Metrics.timer("Store.PullLog.FlushTimer", NONE, NONE).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void flushActionLogTimer(long elapsedMillis) {
        Metrics.timer("Store.MessageLog.FlushTimer", NONE, NONE).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void flushConsumerLogTimer(long elapsedMillis) {
        Metrics.timer("Store.ConsumerLog.FlushTimer", NONE, NONE).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void flushMessageLogTimer(long elapsedMillis) {
        Metrics.timer("Store.MessageLog.FlushTimer", NONE, NONE).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void pullLogFlusherExceedCheckpointIntervalCountInc() {
        countInc("PullLogFlusher.ExceedCheckpointInterval", NONE, NONE);
    }

    public static void pullLogFlusherElapsedPerExecute(long elapsedMillis) {
        Metrics.timer("PullLogFlusher.ElapsedPerExecute", NONE, NONE).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void pullLogFlusherFlushFailedCountInc() {
        countInc("PullLogFlusher.FlushFailed", NONE, NONE);
    }

    public static void consumerLogFlusherExceedCheckpointIntervalCountInc() {
        countInc("ConsumerLogFlusher.ExceedCheckpointInterval", NONE, NONE);
    }

    public static void consumerLogFlusherElapsedPerExecute(long elapsedMillis) {
        Metrics.timer("ConsumerLogFlusher.ElapsedPerExecute", NONE, NONE).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void consumerLogFlusherFlushFailedCountInc() {
        countInc("ConsumerLogFlusher.FlushFailed", NONE, NONE);
    }

    public static void hitDeletedConsumerLogSegmentCountInc(String subject) {
        subjectCountInc("HitDeletedConsumerLogSegment", subject);
    }

    public static void adjustConsumerLogMinOffset(String subject) {
        subjectCountInc("ConsumerLog.AdjustMinOffset", subject);
    }

    public static void nettyRequestExecutorExecuteTimer(long elapsedMillis) {
        Metrics.timer("NettyRequestExecutor.execute", NONE, NONE).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void executorQueueSizeGauge(String requestCode, Supplier<Double> supplier) {
        Metrics.gauge("executorQueueSize", REQUEST_CODE, new String[]{requestCode}, supplier);
    }

    public static void activeConnectionGauge(String name, Supplier<Double> supplier) {
        Metrics.gauge("NettyProvider.Connection.ActiveCount", new String[]{"name"}, new String[]{name}, supplier);
    }

    public static void activeClientCount(Supplier<Double> supplier) {
        Metrics.gauge("Broker.ActiveClientCount", NONE, NONE, supplier);
    }

    public static void messageSequenceLagGauge(String subject, String group, Supplier<Double> supplier) {
        Metrics.gauge("messageSequenceLag", SUBJECT_GROUP_ARRAY, new String[]{subject, group}, supplier);
    }

    public static void replayMessageLogLag(Supplier<Double> supplier) {
        Metrics.gauge("ReplayMessageLogLag", NONE, NONE, supplier);
    }

    public static void replayActionLogLag(Supplier<Double> supplier) {
        Metrics.gauge("ReplayActionLogLag", NONE, NONE, supplier);
    }

    public static void slaveMessageLogLagGauge(String role, Supplier<Double> supplier) {
        Metrics.gauge("slaveMessageLogLag", new String[]{"role"}, new String[]{role}, supplier);
    }

    public static void slaveActionLogLagGauge(String role, Supplier<Double> supplier) {
        Metrics.gauge("slaveActionLogLag", new String[]{"role"}, new String[]{role}, supplier);
    }

    public static void removeMessageSequenceLag(String subject, String group) {
        Metrics.remove("messageSequenceLag", SUBJECT_GROUP_ARRAY, new String[]{subject, group});
    }

    public static void syncTaskExecTimer(String processor, long elapsedMillis) {
        Metrics.timer("SyncTask.ExecTimer", new String[]{"processor"}, new String[]{processor}).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    public static void syncTaskSyncFailedCountInc(String brokerGroup) {
        countInc("SyncTask.SyncFailedCount", new String[]{"BrokerGroup"}, new String[]{brokerGroup});
    }

    public static void dispatchersGauge(String name, Supplier<Double> supplier) {
        Metrics.gauge("dispatchers-" + name, NONE, NONE, supplier);
    }

    public static void actorQueueGauge(String systemName, String name, Supplier<Double> supplier) {
        Metrics.gauge("actor-queue-" + systemName, new String[]{"name"}, new String[]{name}, supplier);
    }

    public static void actorSystemQueueGauge(String name, Supplier<Double> supplier) {
        Metrics.gauge("actorsystem-queue-" + name, NONE, NONE, supplier);
    }

    public static void actorProcessTime(String actor, long elapsedMillis) {
        Metrics.timer("actor-processTime", new String[]{"actor"}, new String[]{actor}).update(elapsedMillis, TimeUnit.MILLISECONDS);
    }
}
