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

package qunar.tc.qmq.store;

import qunar.tc.qmq.configuration.DynamicConfig;
import qunar.tc.qmq.constants.BrokerConstants;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author keli.wang
 * @since 2017/7/13
 */
public class StorageConfigImpl implements StorageConfig {
    private static final String CHECKPOINT = "checkpoint";
    private static final String MESSAGE_LOG = "messagelog";
    private static final String CONSUMER_LOG = "consumerlog";
    private static final String PULL_LOG = "pulllog";
    private static final String ACTION_LOG = "actionlog";

    private static final long MS_PER_HOUR = TimeUnit.HOURS.toMillis(1);

    private final DynamicConfig config;

    public StorageConfigImpl(final DynamicConfig config) {
        this.config = config;
    }

    @Override
    public String getCheckpointStorePath() {
        return buildStorePath(CHECKPOINT);
    }

    @Override
    public String getMessageLogStorePath() {
        return buildStorePath(MESSAGE_LOG);
    }

    @Override
    public long getMessageLogRetentionMs() {
        final int retentionHours = config.getInt(BrokerConstants.MESSAGE_LOG_RETENTION_HOURS, BrokerConstants.DEFAULT_MESSAGE_LOG_RETENTION_HOURS);
        return retentionHours * MS_PER_HOUR;
    }

    @Override
    public String getConsumerLogStorePath() {
        return buildStorePath(CONSUMER_LOG);
    }

    @Override
    public long getConsumerLogRetentionMs() {
        final int retentionHours = config.getInt(BrokerConstants.CONSUMER_LOG_RETENTION_HOURS, BrokerConstants.DEFAULT_CONSUMER_LOG_RETENTION_HOURS);
        return retentionHours * MS_PER_HOUR;
    }

    @Override
    public int getLogRetentionCheckIntervalSeconds() {
        return config.getInt(BrokerConstants.LOG_RETENTION_CHECK_INTERVAL_SECONDS, BrokerConstants.DEFAULT_LOG_RETENTION_CHECK_INTERVAL_SECONDS);
    }

    @Override
    public String getPullLogStorePath() {
        return buildStorePath(PULL_LOG);
    }

    @Override
    public long getPullLogRetentionMs() {
        final int retentionHours = config.getInt(BrokerConstants.PULL_LOG_RETENTION_HOURS, BrokerConstants.DEFAULT_PULL_LOG_RETENTION_HOURS);
        return retentionHours * MS_PER_HOUR;
    }

    @Override
    public String getActionLogStorePath() {
        return buildStorePath(ACTION_LOG);
    }

    private String buildStorePath(final String name) {
        final String root = config.getString(BrokerConstants.STORE_ROOT, BrokerConstants.LOG_STORE_ROOT);
        return new File(root, name).getAbsolutePath();
    }

    @Override
    public boolean isDeleteExpiredLogsEnable() {
        return config.getBoolean(BrokerConstants.ENABLE_DELETE_EXPIRED_LOGS, false);
    }

    @Override
    public long getLogRetentionMs() {
        final int retentionHours = config.getInt(BrokerConstants.PULL_LOG_RETENTION_HOURS, BrokerConstants.DEFAULT_PULL_LOG_RETENTION_HOURS);
        return retentionHours * MS_PER_HOUR;
    }

    @Override
    public int getRetryDelaySeconds() {
        return config.getInt(BrokerConstants.RETRY_DELAY_SECONDS, BrokerConstants.DEFAULT_RETRY_DELAY_SECONDS);
    }

    @Override
    public int getCheckpointRetainCount() {
        return config.getInt(BrokerConstants.CHECKPOINT_RETAIN_COUNT, BrokerConstants.DEFAULT_CHECKPOINT_RETAIN_COUNT);
    }

    @Override
    public long getActionCheckpointInterval() {
        return config.getLong(BrokerConstants.ACTION_CHECKPOINT_INTERVAL, BrokerConstants.DEFAULT_ACTION_CHECKPOINT_INTERVAL);
    }

    @Override
    public long getMessageCheckpointInterval() {
        return config.getLong(BrokerConstants.MESSAGE_CHECKPOINT_INTERVAL, BrokerConstants.DEFAULT_MESSAGE_CHECKPOINT_INTERVAL);
    }
}
