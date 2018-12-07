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

package qunar.tc.qmq.protocol.consumer;

import qunar.tc.qmq.TagType;

import java.util.Collections;
import java.util.List;

/**
 * @author yiqun.fan create on 17-7-4.
 */
public class PullRequest {
    private String subject;
    private String group;
    private int requestNum;
    private long timeoutMillis;
    private long offset;
    private long pullOffsetBegin;
    private long pullOffsetLast;
    private String consumerId;
    private boolean isBroadcast;

    private int tagTypeCode = TagType.NO_TAG.getCode();

    private List<byte[]> tags = Collections.emptyList();

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

    public int getRequestNum() {
        return requestNum;
    }

    public void setRequestNum(int requestNum) {
        this.requestNum = requestNum;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getPullOffsetBegin() {
        return pullOffsetBegin;
    }

    public void setPullOffsetBegin(long pullOffsetBegin) {
        this.pullOffsetBegin = pullOffsetBegin;
    }

    public long getPullOffsetLast() {
        return pullOffsetLast;
    }

    public void setPullOffsetLast(long pullOffsetLast) {
        this.pullOffsetLast = pullOffsetLast;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(boolean broadcast) {
        isBroadcast = broadcast;
    }

    public int getTagTypeCode() {
        return tagTypeCode;
    }

    public void setTagTypeCode(int tagTypeCode) {
        this.tagTypeCode = tagTypeCode;
    }

    public List<byte[]> getTags() {
        return tags;
    }

    public void setTags(List<byte[]> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "PullRequest{" +
                "subject='" + subject + '\'' +
                ", group='" + group + '\'' +
                ", requestNum=" + requestNum +
                ", timeoutMillis=" + timeoutMillis +
                ", offset=" + offset +
                ", pullOffsetBegin=" + pullOffsetBegin +
                ", pullOffsetLast=" + pullOffsetLast +
                ", consumerId='" + consumerId + '\'' +
                ", isBroadcast=" + isBroadcast +
                ", tagTypeCode=" + tagTypeCode +
                ", tags=" + tags +
                '}';
    }

}
