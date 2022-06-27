package com.csd.replica.datalayer;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

public class Block implements Serializable {

    byte[] previousBlockHash;
    byte[] merkleRootHash;
    int difficultyTarget;
    List<String> TXIDs;
    private OffsetDateTime timestamp;
    byte[] proof;

    public Block() {
    }

    public Block(byte[] previousBlockHash, byte[] merkleRootHash, int difficultyTarget, List<String> TXIDs, OffsetDateTime timestamp) {
        this.previousBlockHash = previousBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.difficultyTarget = difficultyTarget;
        this.TXIDs = TXIDs;
        this.timestamp = timestamp;
    }

    public byte[] getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void setPreviousBlockHash(byte[] previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public byte[] getMerkleRootHash() {
        return merkleRootHash;
    }

    public void setMerkleRootHash(byte[] merkleRootHash) {
        this.merkleRootHash = merkleRootHash;
    }

    public int getDifficultyTarget() {
        return difficultyTarget;
    }

    public void setDifficultyTarget(int difficultyTarget) {
        this.difficultyTarget = difficultyTarget;
    }

    public List<String> getTXIDs() {
        return TXIDs;
    }

    public void setTXIDs(List<String> TXIDs) {
        this.TXIDs = TXIDs;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getProof() {
        return proof;
    }

    public void setProof(byte[] proof) {
        this.proof = proof;
    }

    @Override
    public String toString() {
        return "Block{" +
                "previousBlockHash=" + Arrays.toString(previousBlockHash) +
                ", merkleRootHash=" + Arrays.toString(merkleRootHash) +
                ", difficultyTarget=" + difficultyTarget +
                ", nonce=" + Arrays.toString(proof) +
                ", TXIDs=" + TXIDs +
                ", timestamp=" + timestamp +
                '}';
    }
}
