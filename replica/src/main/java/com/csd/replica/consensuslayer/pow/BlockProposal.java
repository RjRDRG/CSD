package com.csd.replica.consensuslayer.pow;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.Request;
import com.csd.common.traits.Signature;
import com.csd.replica.datalayer.Block;

import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class BlockProposal extends Request {

    private Block block;

    public BlockProposal(byte[] clientId, SignatureSuite signatureSuite, Block block) {
        try {
            this.clientId = new byte[][]{clientId};
            this.clientSignature = new Signature[]{
                    new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest()))
            };
            this.nonce = OffsetDateTime.now();
            this.block = block;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BlockProposal() {
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public byte[] serializedRequest() {
        return concat(clientId[0], dataToBytesDeterministic(nonce), dataToBytesDeterministic(block));
    }

    @Override
    public String toString() {
        return "BlockProposal{" +
                "block=" + block +
                '}';
    }
}
