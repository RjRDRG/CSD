package com.csd.proxy.impl.pow;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import com.csd.common.cryptography.key.IKeyRegistry;
import com.csd.common.cryptography.key.ExperimentalKeyRegistry;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.response.wrapper.ReplicaResponse;
import com.csd.common.traits.Signature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static com.csd.common.util.Serialization.*;

public class PowReplyListener implements ReplyListener {
    private final AsynchServiceProxy serviceProxy;

    private final IKeyRegistry pubKeyRegistry;

    private final CountDownLatch latch;
    private double q;
    private Map<String, List<TOMMessage>> responses = new ConcurrentHashMap<>();
    private ConsensusResponse response = null;
    private List<ReplicaResponse> replicaResponses;

    public PowReplyListener(AsynchServiceProxy serviceProxy, CountDownLatch latch) {
        this.serviceProxy = serviceProxy;
        this.pubKeyRegistry = new ExperimentalKeyRegistry();
        this.latch = latch;
        this.q = Math.ceil((double) (serviceProxy.getViewManager().getCurrentViewN() + serviceProxy.getViewManager().getCurrentViewF() + 1) / 3.0);
        this.replicaResponses = new ArrayList<>();
    }

    @Override
    public void reset() {
        q = Math.ceil((double) (serviceProxy.getViewManager().getCurrentViewN() + serviceProxy.getViewManager().getCurrentViewF() + 1) / 3.0);
        responses = new ConcurrentHashMap<>();
        response = null;
        replicaResponses = new ArrayList<>();
    }

    @Override
    public void replyReceived(RequestContext requestContext, TOMMessage tomMessage) {
        ConsensusResponse response = bytesToData(tomMessage.getContent());

        List<TOMMessage> l = responses.computeIfAbsent(dataToJson(response.getEncodedResult()), k -> new LinkedList<>());
        l.add(tomMessage);
        this.replicaResponses.add(new ReplicaResponse(
                tomMessage.getSender(),
                tomMessage.serializedMessage,
                new Signature(pubKeyRegistry.getReplicaKey(tomMessage.getSender()), tomMessage.serializedMessageSignature))
        );

        if (l.size() > q) {
            this.response = response;
            latch.countDown();
            serviceProxy.cleanAsynchRequest(requestContext.getOperationId());
        }
    }

    public ConsensusResponse getResponse() {
        return response;
    }

    public List<ReplicaResponse> getReplicaResponses() {
        return replicaResponses;
    }
}
