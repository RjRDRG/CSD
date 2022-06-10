package com.csd.proxy.impl;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.traits.Signature;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.bytesToData;

public class LedgerReplyListener implements ReplyListener {
    private final AsynchServiceProxy serviceProxy;
    private final Thread callerThread;
    private double q;
    private Map<ConsensusResponse, List<TOMMessage>> responses = new ConcurrentHashMap<>();
    private ConsensusResponse response = null;

    public LedgerReplyListener(AsynchServiceProxy serviceProxy, Thread callerThread) {
        this.serviceProxy = serviceProxy;
        this.callerThread = callerThread;
        this.q = Math.ceil((double) (serviceProxy.getViewManager().getCurrentViewN() + serviceProxy.getViewManager().getCurrentViewF() + 1) / 3.0);
    }

    @Override
    public void reset() {
        q = Math.ceil((double) (serviceProxy.getViewManager().getCurrentViewN() + serviceProxy.getViewManager().getCurrentViewF() + 1) / 3.0);
        responses = new ConcurrentHashMap<>();
        response = null;
    }

    @Override
    public void replyReceived(RequestContext requestContext, TOMMessage tomMessage) {
        System.out.println("\n\n\n\\nn\nadasdasdassdasdasdas\n\n\n\n");
        ConsensusResponse response = bytesToData(tomMessage.getContent());
        System.out.println("\n\n\n\\nn\n#########################\n\n\n\n");
        List<TOMMessage> l = responses.computeIfAbsent(response, k -> new LinkedList<>());
        l.add(tomMessage);
        if (l.size() >= q) {
            this.response = response;
            callerThread.notify();
            serviceProxy.cleanAsynchRequest(requestContext.getOperationId());
        }
    }

    public ConsensusResponse getResponse() {
        return response;
    }

    public List<Signature> getResponseSignatures() {
        return responses.get(response).stream().map(t -> new Signature((SignatureSuite) null, t.serializedMessageSignature)).collect(Collectors.toList()); /*TODO get public key*/
    }
}
