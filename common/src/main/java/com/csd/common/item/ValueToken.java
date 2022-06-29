package com.csd.common.item;

import com.csd.common.response.wrapper.ReplicaResponse;

import java.io.Serializable;
import java.util.List;

public class ValueToken implements Serializable {

    private PrivateValueAsset privateValueAsset;

    List<ReplicaResponse> replicaResponsesAndSignatures;

    public ValueToken(String id, byte[] encryptedAmount, double amount, List<ReplicaResponse> replicaResponsesAndSignatures) {
        this.privateValueAsset = new PrivateValueAsset(id,encryptedAmount,amount);
        this.replicaResponsesAndSignatures = replicaResponsesAndSignatures;
    }

    public ValueToken() {
    }

    public PrivateValueAsset getPrivateValueAsset() {
        return privateValueAsset;
    }

    public void setPrivateValueAsset(PrivateValueAsset privateValueAsset) {
        this.privateValueAsset = privateValueAsset;
    }

    public List<ReplicaResponse> getReplicaResponsesAndSignatures() {
        return replicaResponsesAndSignatures;
    }

    public void setReplicaResponsesAndSignatures(List<ReplicaResponse> replicaResponsesAndSignatures) {
        this.replicaResponsesAndSignatures = replicaResponsesAndSignatures;
    }

    @Override
    public String toString() {
        return "ValueToken{" +
                "privateValueAsset=" + privateValueAsset +
                ", replicaResponses=" + replicaResponsesAndSignatures +
                '}';
    }
}
