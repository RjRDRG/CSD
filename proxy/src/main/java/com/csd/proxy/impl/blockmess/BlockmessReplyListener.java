package com.csd.proxy.impl.blockmess;


import applicationInterface.ReplyListener;
import com.csd.common.response.wrapper.ConsensusResponse;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.CountDownLatch;

import static com.csd.common.util.Serialization.bytesToData;

public class BlockmessReplyListener implements ReplyListener {

    private final CountDownLatch latch;
    private final double q;
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
        this.response = bytesToData(pair.getKey());
        latch.countDown();
    }
}
