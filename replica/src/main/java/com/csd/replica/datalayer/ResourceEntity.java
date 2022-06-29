package com.csd.replica.datalayer;

import com.csd.common.item.Resource;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.stringToBytes;

@Entity
public class ResourceEntity implements Serializable {

    private @Id @GeneratedValue long id;
    private long block;
    private String owner;
    private String type;
    private String asset;
    private boolean spent;
    private OffsetDateTime timestamp;
    private String requestSignature;

    public ResourceEntity() {}

    public ResourceEntity(long block, byte[] owner, String type, String asset, Boolean spent, OffsetDateTime timestamp, byte[] requestSignature) {
        this.block = block;
        this.owner = bytesToString(owner);
        this.type = type;
        this.asset = asset;
        this.spent = spent;
        this.timestamp = timestamp;
        this.requestSignature = bytesToString(requestSignature);
    }

    public ResourceEntity(Resource resource) {
        this.id = resource.getId();
        this.block = resource.getBlock();
        this.owner = bytesToString(resource.getOwner());
        this.type = resource.getType().name();
        this.asset = resource.getAsset();
        this.spent = resource.isSpent();
        this.timestamp = resource.getTimestamp();
        this.requestSignature = bytesToString(resource.getRequestSignature());
    }

    public Resource toItem() {
        return new Resource(
                id,
                block,
                stringToBytes(owner),
                Resource.Type.valueOf(type),
                asset,
                spent,
                timestamp,
                stringToBytes(requestSignature)
        );
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public void setTimestamp(OffsetDateTime date) {
        this.timestamp = date;
    }

    public String getRequestSignature() {
        return requestSignature;
    }

    public void setRequestSignature(String requestSignature) {
        this.requestSignature = requestSignature;
    }

    @Override
    public String toString() {
        return "ResourceEntity{" +
                "id=" + id +
                ", block=" + block +
                ", owner='" + owner + '\'' +
                ", type='" + type + '\'' +
                ", asset='" + asset + '\'' +
                ", spent=" + spent +
                ", timestamp=" + timestamp +
                ", requestSignature='" + requestSignature + '\'' +
                '}';
    }
}