package com.csd.common.item;

import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.dataToJson;

public class Transaction implements Serializable {

    private long id;
    private byte[] owner;
    private double amount;
    private OffsetDateTime timestamp;

    public Transaction(long id, byte[] owner, double amount, OffsetDateTime timestamp) {
        this.id = id;
        this.owner = owner;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public Transaction() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getOwner() {
        return owner;
    }

    public void setOwner(byte[] owner) {
        this.owner = owner;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "\nTransaction " + dataToJson(this);
    }
}