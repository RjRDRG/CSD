package com.csd.replica.consensuslayer.pow;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.Request;
import com.csd.common.traits.Signature;
import com.csd.replica.datalayer.Block;
import com.csd.replica.datalayer.Transaction;

import java.time.OffsetDateTime;
import java.util.HashMap;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class BlockProposal extends Request {

    private Block block;
    private Transaction coinbase;

    public BlockProposal(byte[] clientId, SignatureSuite signatureSuite, Block block, Transaction coinbase) {
        this.coinbase = coinbase;
        try {
            this.clientId = new HashMap<>();
            this.clientId.put(0,clientId);
            this.clientSignature = new HashMap<>();
            this.clientSignature.put(0, new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest())));
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

    public Transaction getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(Transaction coinbase) {
        this.coinbase = coinbase;
    }

    @Override
    public byte[] serializedRequest() {
        return concat(clientId.get(0), dataToBytesDeterministic(nonce), dataToBytesDeterministic(block), dataToBytesDeterministic(coinbase));
    }

    @Override
    public String toString() {
        return "BlockProposal{" +
                "block=" + block +
                ", coinbase=" + coinbase +
                '}';
    }
}
