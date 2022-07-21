package com.csd.replica.consensuslayer.pow;

import com.csd.common.traits.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuerThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(QueuerThread.class);

    PowOrderer orderer;

    public QueuerThread(PowOrderer orderer) {
        this.orderer = orderer;
    }

    @Override
    public void run(){
        try {
            Thread.sleep(5000);
            while (true) {
                if(orderer.blockProposalPoll.size() > 0) {
                    BlockProposal blockProposal = orderer.blockProposalPoll.poll();
                    log.info("Evaluating block: " + blockProposal.getBlock().getTimestamp());
                    Result<Long> result = orderer.blockProposalValidator(blockProposal);
                    log.info("Proposal result: " + result);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            throw new RuntimeException(e);
        }
    }

}
