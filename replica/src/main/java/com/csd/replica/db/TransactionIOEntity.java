package com.csd.replica.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
public class TransactionIOEntity implements Serializable {

    private @Id @GeneratedValue long id;

    private long blockId;
    private long transactionId;
    private String owner;
    private long value;
    private OffsetDateTime timestamp;

    public TransactionIOEntity() {}

    public TransactionIOEntity(long blockId, long transactionId, String owner, long value, OffsetDateTime timestamp) {
        this.blockId = blockId;
        this.transactionId = transactionId;
        this.owner = owner;
        this.value = value;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBlockId() {
        return blockId;
    }

    public void setBlockId(long blockId) {
        this.blockId = blockId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}