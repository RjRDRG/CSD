package com.csd.replica.datalayer;

import com.csd.common.cryptography.suites.digest.IDigestSuite;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

@Entity
public class BlockHeaderEntity implements Serializable {

    private @Id @GeneratedValue long id;
    private OffsetDateTime timestamp;
    byte[] previousBlockHash;
    byte[] merkleRootHash;
    int difficultyTarget;
    byte[] nonce;

    public BlockHeaderEntity() {}

    public byte[] getDigest(IDigestSuite suite) {
        try {
            return suite.digest(concat(
                    dataToBytesDeterministic(id),
                    dataToBytesDeterministic(timestamp),
                    previousBlockHash,
                    merkleRootHash,
                    dataToBytesDeterministic(difficultyTarget),
                    nonce
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}