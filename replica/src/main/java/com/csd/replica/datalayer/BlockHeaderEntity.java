package com.csd.replica.datalayer;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

@Entity
public class BlockHeaderEntity implements Serializable {

    private @Id long id;
    private OffsetDateTime timestamp;
    byte[] previousBlockHash;
    byte[] merkleRootHash;
    int difficultyTarget;
    byte[] proof;
    byte[] hash;

    public BlockHeaderEntity() {}

    public BlockHeaderEntity(long id, OffsetDateTime timestamp, byte[] previousBlockHash, byte[] merkleRootHash, int difficultyTarget, byte[] proof) {
        this.id = id;
        this.timestamp = timestamp;
        this.previousBlockHash = previousBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.difficultyTarget = difficultyTarget;
        this.proof = proof;
    }

    public byte[] serializedBlock() {
        return concat(
                dataToBytesDeterministic(timestamp),
                previousBlockHash,
                merkleRootHash,
                dataToBytesDeterministic(difficultyTarget),
                proof
        );
    }

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

    public byte[] getProof() {
        return proof;
    }

    public void setProof(byte[] proof) {
        this.proof = proof;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }
}