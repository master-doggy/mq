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

package qunar.tc.qmq.meta.store;

import qunar.tc.qmq.meta.model.ClientOfflineState;

import java.util.List;
import java.util.Optional;

/**
 * yiqun.fan@qunar.com 2018/2/28
 */
public interface ClientOfflineStore {

    long now();

    Optional<ClientOfflineState> select(String clientId, String subject, String consumerGroup);

    List<ClientOfflineState> selectAll();

    void insertOrUpdate(ClientOfflineState clientState);

    void delete(String subject, String consumerGroup);

    void delete(String clientId, String subject, String consumerGroup);
}
