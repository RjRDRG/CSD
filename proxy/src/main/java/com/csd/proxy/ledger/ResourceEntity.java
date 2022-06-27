package com.csd.proxy.ledger;

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
    private Long block;
    private String owner;
    private String asset;
    private OffsetDateTime timestamp;
    private String requestSignature;

    public ResourceEntity() {}

    public ResourceEntity(byte[] owner, String asset, OffsetDateTime timestamp, byte[] requestSignature) {
        this.owner = bytesToString(owner);
        this.asset = asset;
        this.timestamp = timestamp;
        this.requestSignature = bytesToString(requestSignature);
    }

    public ResourceEntity(Resource resource) {
        this.id = resource.getId();
        this.block = resource.getBlock();
        this.owner = bytesToString(resource.getOwner());
        this.asset = resource.getAmount();
        this.timestamp = resource.getTimestamp();
        this.requestSignature = bytesToString(resource.getRequestSignature());
    }

    public Resource toItem() {
        return new Resource(
                id,
                block,
                stringToBytes(owner),
                asset,
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

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
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
                ", asset=" + asset +
                ", timestamp=" + timestamp +
                ", requestSignature='" + requestSignature + '\'' +
                '}';
    }
}