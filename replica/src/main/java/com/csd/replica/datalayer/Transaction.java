package com.csd.replica.datalayer;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

import static com.csd.common.util.Serialization.bytesToHex;

public class Transaction implements Serializable {

    public enum Type {Value, PrivateValue, Claim}
    private String id;

    private Type type;
    private byte[] owner;
    private byte[] recipient;
    private Object asset;
    private Object fee;
    private OffsetDateTime timestamp;
    private byte[] requestSignature;

    public Transaction() {
    }

    public Transaction(String id, Type type, byte[] owner, byte[] recipient, Object asset, Object fee, OffsetDateTime timestamp, byte[] requestSignature) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.recipient = recipient;
        this.asset = asset;
        this.fee = fee;
        this.timestamp = timestamp;
        this.requestSignature = requestSignature;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public byte[] getOwner() {
        return owner;
    }

    public void setOwner(byte[] owner) {
        this.owner = owner;
    }

    public byte[] getRecipient() {
        return recipient;
    }

    public void setRecipient(byte[] recipient) {
        this.recipient = recipient;
    }

    public Object getAsset() {
        return asset;
    }

    public void setAsset(Object asset) {
        this.asset = asset;
    }

    public Object getFee() {
        return fee;
    }

    public void setFee(Object fee) {
        this.fee = fee;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getRequestSignature() {
        return requestSignature;
    }

    public void setRequestSignature(byte[] requestSignature) {
        this.requestSignature = requestSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                "type=" + type.name() +
                "owner=" + bytesToHex(owner) +
                ", recipient=" + bytesToHex(recipient) +
                ", asset='" + asset + '\'' +
                ", fee='" + fee + '\'' +
                ", timestamp=" + timestamp +
                ", requestSignature='" + bytesToHex(requestSignature) + '\'' +
                '}';
    }
}
