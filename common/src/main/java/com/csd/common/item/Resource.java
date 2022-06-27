package com.csd.common.item;

import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.dataToJson;

public class Resource implements Serializable {

    private long id;
    private Long block;
    private byte[] owner;
    private String amount;
    private OffsetDateTime timestamp;
    private byte[] requestSignature;

    public Resource(long id, Long block, byte[] owner, String amount, OffsetDateTime timestamp, byte[] requestSignature) {
        this.id = id;
        this.block = block;
        this.owner = owner;
        this.amount = amount;
        this.timestamp = timestamp;
        this.requestSignature = requestSignature;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getBlock() {
        return block;
    }

    public void setBlock(Long block) {
        this.block = block;
    }

    public byte[] getOwner() {
        return owner;
    }

    public void setOwner(byte[] owner) {
        this.owner = owner;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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
        return "\nTransaction " + dataToJson(this);
    }
}