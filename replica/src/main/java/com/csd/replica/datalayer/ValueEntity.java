package com.csd.replica.datalayer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class ValueEntity implements Serializable {

    private @Id @GeneratedValue long id;
    private long blockId;
    private long txid;
    private String owner;
    private long value;

    public ValueEntity(long id, long blockId, long txid, String owner, long value) {
        this.id = id;
        this.blockId = blockId;
        this.txid = txid;
        this.owner = owner;
        this.value = value;
    }

    public ValueEntity() {}

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

    public long getTxid() {
        return txid;
    }

    public void setTxid(long txid) {
        this.txid = txid;
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
}