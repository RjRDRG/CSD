package com.csd.common.item;

import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.dataToJson;

public class Resource implements Serializable {

    public enum Type {VALUE, CRYPT}

    private long id;
    private Long block;
    private byte[] owner;

    private Type type;
    private String asset;

    private boolean spent;

    private OffsetDateTime timestamp;
    private byte[] requestSignature;

    public Resource(long id, Long block, byte[] owner, Type type, String asset, boolean spent, OffsetDateTime timestamp, byte[] requestSignature) {
        this.id = id;
        this.block = block;
        this.owner = owner;
        this.type = type;
        this.asset = asset;
        this.spent = spent;
        this.timestamp = timestamp;
        this.requestSignature = requestSignature;
    }

    public Resource() {
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public boolean isSpent() {
        return spent;
    }

    public void setSpent(boolean spent) {
        this.spent = spent;
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
        return "\nResource " + dataToJson(this);
    }
}