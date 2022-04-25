package com.csd.common.item;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.dataToJson;

public class Transaction implements Serializable {

    private long id;
    private byte[] owner;
    private double amount;
    private OffsetDateTime timestamp;
    private byte[] hashPreviousTransaction;

    public Transaction(long id, byte[] owner, double amount, OffsetDateTime timestamp, byte[] hashPreviousTransaction) {
        this.id = id;
        this.owner = owner;
        this.amount = amount;
        this.timestamp = timestamp;
        this.hashPreviousTransaction = hashPreviousTransaction;
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

    public byte[] getHashPreviousTransaction() {
        return hashPreviousTransaction;
    }

    public void setHashPreviousTransaction(byte[] hashPreviousTransaction) {
        this.hashPreviousTransaction = hashPreviousTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id && Double.compare(that.amount, amount) == 0 && Arrays.equals(owner, that.owner) && timestamp.equals(that.timestamp) && Arrays.equals(hashPreviousTransaction, that.hashPreviousTransaction);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, amount, timestamp);
        result = 31 * result + Arrays.hashCode(owner);
        result = 31 * result + Arrays.hashCode(hashPreviousTransaction);
        return result;
    }

    @Override
    public String toString() {
        return "\nTransaction " + dataToJson(this);
    }
}