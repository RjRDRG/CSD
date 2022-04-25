package com.csd.proxy.db;

import com.csd.common.item.Transaction;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.stringToBytes;

@Entity
public class TransactionEntity implements Serializable {

    private @Id long id;
    private String owner;
    private double amount;
    private OffsetDateTime timestamp;
    private String hashPreviousTransaction;

    public TransactionEntity() {}

    public TransactionEntity(Transaction transaction) {
        this.id = transaction.getId();
        this.owner = bytesToString(transaction.getOwner());
        this.amount = transaction.getAmount();
        this.timestamp = transaction.getTimestamp();
        this.hashPreviousTransaction = bytesToString(transaction.getHashPreviousTransaction());
    }

    public Transaction toItem() {
        return new Transaction(
                id,
                stringToBytes(owner),
                amount,
                timestamp,
                stringToBytes(hashPreviousTransaction)
        );
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
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

    public void setTimestamp(OffsetDateTime date) {
        this.timestamp = date;
    }

    public String getHashPreviousTransaction() {
        return hashPreviousTransaction;
    }

    public void setHashPreviousTransaction(String previousTransactionHash) {
        this.hashPreviousTransaction = previousTransactionHash;
    }

    @Override
    public String toString() {
        return "TransactionEntity{" +
                "id='" + id + '\'' +
                ", owner='" + owner + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", previousTransactionHash='" + hashPreviousTransaction + '\'' +
                '}';
    }
}