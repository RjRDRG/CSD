package com.csd.replica.consensuslayer.pow;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.core.messages.TOMMessageType;
import com.csd.common.item.Wallet;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.replica.datalayer.Block;

import java.util.UUID;

import static com.csd.common.util.Serialization.dataToBytes;

public class ReplicaBroadcast extends AsynchServiceProxy {

    public ReplicaBroadcast(int processId) throws Exception {
        super(processId);
    }

    @SuppressWarnings("unchecked")
    public void broadcast(BlockProposal blockProposal) {
        try {
            ConsensusRequest consensusRequest = new ConsensusRequest(blockProposal, ConsensusRequest.Type.BLOCK, -1);

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

