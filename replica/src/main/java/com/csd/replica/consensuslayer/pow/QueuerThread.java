package com.csd.replica.consensuslayer.pow;

import com.csd.common.traits.Result;

public class QueuerThread extends Thread {

    PowOrderer orderer;

    public QueuerThread(PowOrderer orderer) {
        this.orderer = orderer;
    }

    @Override
    public void run(){
        while (true) {
            if(orderer.blockProposalPoll.size() > 0) {
                BlockProposal blockProposal = orderer.blockProposalPoll.poll();
                Result<Long> result = orderer.blockProposalValidator(blockProposal);
                System.out.println("=========================================> " + result);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
