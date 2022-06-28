package com.csd.replica.consensuslayer.pow;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.core.messages.TOMMessageType;
import com.csd.common.item.Wallet;
import com.csd.common.request.Request;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.response.wrapper.Response;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.csd.replica.datalayer.Block;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.dataToBytes;

public class ReplicaBroadcast extends AsynchServiceProxy {

    private final Wallet wallet;

    public ReplicaBroadcast(int processId) throws Exception {
        super(processId);
        wallet = new Wallet(processId + "@miner", UUID.randomUUID().toString());
    }

    @SuppressWarnings("unchecked")
    public void broadcast(Block block) {
        try {
            BlockProposal blockProposal = new BlockProposal(wallet.clientId, wallet.signatureSuite, block);

            ConsensusRequest consensusRequest = new ConsensusRequest(blockProposal, ConsensusRequest.Type.BLOCK, 0);

            super.invokeAsynchRequest(dataToBytes(consensusRequest), new ReplyListener() {
                @Override
                public void reset() {}
                @Override
                public void replyReceived(RequestContext requestContext, TOMMessage tomMessage) {}
            }, TOMMessageType.ORDERED_REQUEST);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

