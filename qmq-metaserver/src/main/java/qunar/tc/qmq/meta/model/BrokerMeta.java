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

package qunar.tc.qmq.meta.model;

import qunar.tc.qmq.meta.BrokerRole;

/**
 * @author keli.wang
 * @since 2018-11-29
 */
public class BrokerMeta {
    private final String group;
    private final BrokerRole role;
    private final String hostname;
    private final String ip;
    private final int servePort;
    private final int syncPort;

    public BrokerMeta(final String group, final BrokerRole role, final String hostname, final String ip, final int servePort, final int syncPort) {
        this.group = group;
        this.role = role;
        this.hostname = hostname;
        this.ip = ip;
        this.servePort = servePort;
        this.syncPort = syncPort;
    }

    public String getGroup() {
        return group;
    }

    public BrokerRole getRole() {
        return role;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public int getServePort() {
        return servePort;
    }

    public int getSyncPort() {
        return syncPort;
    }
}
