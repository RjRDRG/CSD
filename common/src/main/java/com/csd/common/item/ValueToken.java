package com.csd.common.item;

import com.csd.common.response.wrapper.ReplicaResponse;

import java.io.Serializable;
import java.util.List;

public class ValueToken implements Serializable {

    private PrivateValueAsset privateValueAsset;

    List<ReplicaResponse> replicaResponses;

    public ValueToken(String id, byte[] encryptedAmount, double amount, List<ReplicaResponse> replicaResponses) {
        this.privateValueAsset = new PrivateValueAsset(id,encryptedAmount,amount);
        this.replicaResponses = replicaResponses;
    }

    public ValueToken() {
    }

    public PrivateValueAsset getPrivateValueAsset() {
        return privateValueAsset;
    }

    public void setPrivateValueAsset(PrivateValueAsset privateValueAsset) {
        this.privateValueAsset = privateValueAsset;
    }

    public List<ReplicaResponse> getReplicaResponses() {
        return replicaResponses;
    }

    public void setReplicaResponses(List<ReplicaResponse> replicaResponses) {
        this.replicaResponses = replicaResponses;
    }

    @Override
    public String toString() {
        return "ValueToken{" +
                "privateValueAsset=" + privateValueAsset +
                ", replicaResponses=" + replicaResponses +
                '}';
    }
}
