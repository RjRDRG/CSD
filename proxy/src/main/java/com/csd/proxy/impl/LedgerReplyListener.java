package com.csd.proxy.impl;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.traits.Signature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.*;

public class LedgerReplyListener implements ReplyListener {
    private final AsynchServiceProxy serviceProxy;

    private final CountDownLatch latch;
    private double q;
    private Map<String, List<TOMMessage>> responses = new ConcurrentHashMap<>();
    private ConsensusResponse response = null;
    private List<Signature> signatures;

    public LedgerReplyListener(AsynchServiceProxy serviceProxy, CountDownLatch latch) {
        this.serviceProxy = serviceProxy;
        this.latch = latch;
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
        List<TOMMessage> l = responses.computeIfAbsent(bytesToHex(tomMessage.getContent()), k -> new LinkedList<>());
        l.add(tomMessage);
        System.out.println("\n\n\n\n --" + tomMessage.signed + Arrays.toString(tomMessage.serializedMessageSignature) + "\n" + Arrays.toString(tomMessage.serializedMessageMAC));
        if (l.size() > q) {
            this.response = bytesToData(tomMessage.getContent());
            this.signatures = l.stream().map(t -> new Signature((EncodedPublicKey) null, t.serializedMessageSignature)).collect(Collectors.toList()); /*TODO get public key*/
            latch.countDown();
            serviceProxy.cleanAsynchRequest(requestContext.getOperationId());
        }
    }

    public ConsensusResponse getResponse() {
        return response;
    }

    public List<Signature> getResponseSignatures() {
        return signatures;
    }
}
