package com.csd.proxy.impl.blockmess;


import applicationInterface.ReplyListener;
import com.csd.common.response.wrapper.ConsensusResponse;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static com.csd.common.util.Serialization.bytesToData;
import static com.csd.common.util.Serialization.bytesToHex;

public class BlockmessReplyListener implements ReplyListener {

    private final CountDownLatch latch;
    private final double q;
    private final Map<String, List<byte[]>> responses = new ConcurrentHashMap<>();
    private ConsensusResponse response = null;

    public BlockmessReplyListener(CountDownLatch latch, int quorum) {
        this.latch = latch;
        this.q = quorum;
    }

    public ConsensusResponse getResponse() {
        return response;
    }

    @Override
    public void processReply(Pair<byte[], Long> pair) {
        System.out.println("received");
        List<byte[]> l = responses.computeIfAbsent(bytesToHex(pair.getKey()), k -> new LinkedList<>());
        l.add(pair.getKey());
        if (l.size() > q && response == null) {
            this.response = bytesToData(pair.getKey());
            latch.countDown();
        }
    }
}
