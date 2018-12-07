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

package qunar.tc.qmq.meta.processor;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qmq.base.ClientRequestType;
import qunar.tc.qmq.base.OnOfflineState;
import qunar.tc.qmq.concurrent.ActorSystem;
import qunar.tc.qmq.meta.BrokerCluster;
import qunar.tc.qmq.meta.BrokerGroup;
import qunar.tc.qmq.meta.BrokerState;
import qunar.tc.qmq.meta.cache.CachedOfflineStateManager;
import qunar.tc.qmq.meta.route.SubjectRouter;
import qunar.tc.qmq.meta.store.Store;
import qunar.tc.qmq.meta.utils.ClientLogUtils;
import qunar.tc.qmq.protocol.CommandCode;
import qunar.tc.qmq.protocol.Datagram;
import qunar.tc.qmq.protocol.PayloadHolder;
import qunar.tc.qmq.protocol.RemotingHeader;
import qunar.tc.qmq.protocol.consumer.MetaInfoRequest;
import qunar.tc.qmq.protocol.consumer.MetaInfoResponse;
import qunar.tc.qmq.util.RemotingBuilder;
import qunar.tc.qmq.utils.PayloadHolderUtils;
import qunar.tc.qmq.utils.RetrySubjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yunfeng.yang
 * @since 2017/9/1
 */
class ClientRegisterWorker implements ActorSystem.Processor<ClientRegisterProcessor.ClientRegisterMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(ClientRegisterProcessor.class);

    private final SubjectRouter subjectRouter;
    private final ActorSystem actorSystem;
    private final Store store;
    private final CachedOfflineStateManager offlineStateManager;

    ClientRegisterWorker(final SubjectRouter subjectRouter, final CachedOfflineStateManager offlineStateManager, final Store store) {
        this.subjectRouter = subjectRouter;
        this.actorSystem = new ActorSystem("qmq-meta");
        this.offlineStateManager = offlineStateManager;
        this.store = store;
    }

    void register(ClientRegisterProcessor.ClientRegisterMessage message) {
        actorSystem.dispatch("client-register-" + message.getMetaInfoRequest().getSubject(), message, this);
    }

    @Override
    public boolean process(ClientRegisterProcessor.ClientRegisterMessage message, ActorSystem.Actor<ClientRegisterProcessor.ClientRegisterMessage> self) {
        final MetaInfoRequest request = message.getMetaInfoRequest();

        final MetaInfoResponse response = handleClientRegister(request);
        writeResponse(message, response);
        return true;
    }

    private MetaInfoResponse handleClientRegister(final MetaInfoRequest request) {
        final String realSubject = RetrySubjectUtils.getRealSubject(request.getSubject());
        final int clientRequestType = request.getRequestType();

        try {
            if (ClientRequestType.ONLINE.getCode() == clientRequestType) {
                store.insertClientMetaInfo(request);
            }

            final List<BrokerGroup> brokerGroups = subjectRouter.route(realSubject, request);
            final List<BrokerGroup> filteredBrokerGroups = filterBrokerGroups(brokerGroups);
            final OnOfflineState clientState = offlineStateManager.queryClientState(request.getClientId(), request.getSubject(), request.getConsumerGroup());

            ClientLogUtils.log(realSubject,
                    "client register response, request:{}, realSubject:{}, brokerGroups:{}, clientState:{}",
                    request, realSubject, filteredBrokerGroups, clientState);

            return buildResponse(request, offlineStateManager.getLastUpdateTimestamp(), clientState, new BrokerCluster(filteredBrokerGroups));
        } catch (Exception e) {
            LOG.error("process exception. {}", request, e);
            return buildResponse(request, -2, OnOfflineState.OFFLINE, new BrokerCluster(new ArrayList<>()));
        }
    }

    private List<BrokerGroup> filterBrokerGroups(final List<BrokerGroup> brokerGroups) {
        return removeNrwBrokerGroup(brokerGroups);
    }

    private List<BrokerGroup> removeNrwBrokerGroup(final List<BrokerGroup> brokerGroups) {
        if (brokerGroups.isEmpty()) {
            return brokerGroups;
        }

        final List<BrokerGroup> result = new ArrayList<>();
        for (final BrokerGroup brokerGroup : brokerGroups) {
            if (brokerGroup.getBrokerState() != BrokerState.NRW) {
                result.add(brokerGroup);
            }
        }
        return result;
    }

    private MetaInfoResponse buildResponse(MetaInfoRequest clientRequest, long updateTime, OnOfflineState clientState, BrokerCluster brokerCluster) {
        final MetaInfoResponse response = new MetaInfoResponse();
        response.setTimestamp(updateTime);
        response.setOnOfflineState(clientState);
        response.setSubject(clientRequest.getSubject());
        response.setConsumerGroup(clientRequest.getConsumerGroup());
        response.setClientTypeCode(clientRequest.getClientTypeCode());
        response.setBrokerCluster(brokerCluster);
        return response;
    }

    private void writeResponse(final ClientRegisterProcessor.ClientRegisterMessage message, final MetaInfoResponse response) {
        final RemotingHeader header = message.getHeader();
        final MetaInfoResponsePayloadHolder payloadHolder = new MetaInfoResponsePayloadHolder(response, header.getVersion());
        final Datagram datagram = RemotingBuilder.buildResponseDatagram(CommandCode.SUCCESS, header, payloadHolder);
        message.getCtx().writeAndFlush(datagram);
    }

    private static class MetaInfoResponsePayloadHolder implements PayloadHolder {
        private final MetaInfoResponse response;
        private final short version;

        MetaInfoResponsePayloadHolder(MetaInfoResponse response, short version) {
            this.response = response;
            this.version = version;
        }

        @Override
        public void writeBody(ByteBuf out) {
            out.writeLong(response.getTimestamp());
            PayloadHolderUtils.writeString(response.getSubject(), out);
            PayloadHolderUtils.writeString(response.getConsumerGroup(), out);
            out.writeByte(response.getOnOfflineState().code());
            out.writeByte(response.getClientTypeCode());
            out.writeShort(response.getBrokerCluster().getBrokerGroups().size());
            writeBrokerCluster(out);
        }

        private void writeBrokerCluster(ByteBuf out) {
            for (BrokerGroup brokerGroup : response.getBrokerCluster().getBrokerGroups()) {
                PayloadHolderUtils.writeString(brokerGroup.getGroupName(), out);
                PayloadHolderUtils.writeString(brokerGroup.getMaster(), out);
                out.writeLong(brokerGroup.getUpdateTime());
                out.writeByte(brokerGroup.getBrokerState().getCode());
            }
        }
    }
}
