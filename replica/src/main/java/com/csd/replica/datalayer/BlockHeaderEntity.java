package com.csd.replica.datalayer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
public class BlockHeaderEntity implements Serializable {

    private @Id @GeneratedValue long id;
    private OffsetDateTime timestamp;
    byte[] previousBlockHash;
    byte[] merkleRootHash;
    int difficultyTarget;
    byte[] nonce;

    public BlockHeaderEntity() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
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

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}