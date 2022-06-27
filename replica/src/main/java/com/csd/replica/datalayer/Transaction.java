package com.csd.replica.datalayer;

import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.bytesToHex;

public class Transaction implements Serializable {

    private String id;

    private byte[] owner;
    private byte[] recipient;
    private Object asset;
    private Object fee;
    private OffsetDateTime timestamp;
    private byte[] requestSignature;

    public Transaction() {
    }

    public Transaction(String id, byte[] owner, byte[] recipient, Object asset, Object fee, OffsetDateTime timestamp, byte[] requestSignature) {
        this.id = id;
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
    public String toString() {
        return "Transaction{" +
                "owner=" + bytesToHex(owner) +
                ", recipient=" + bytesToHex(recipient) +
                ", asset='" + asset + '\'' +
                ", fee='" + fee + '\'' +
                ", timestamp=" + timestamp +
                ", requestSignature='" + bytesToHex(requestSignature) + '\'' +
                '}';
    }
}
