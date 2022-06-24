package com.csd.common.response.wrapper;

import com.csd.common.traits.Signature;

import java.io.Serializable;

public class ReplicaResponse implements Serializable {
    int replicaId;
    byte[] serializedMessage;
    Signature signature;

    public ReplicaResponse(int replicaId, byte[] serializedMessage, Signature signature) {
        this.replicaId = replicaId;
        this.serializedMessage = serializedMessage;
        this.signature = signature;
    }

    public ReplicaResponse() {
    }

    public int getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    public byte[] getSerializedMessage() {
        return serializedMessage;
    }

    public void setSerializedMessage(byte[] serializedMessage) {
        this.serializedMessage = serializedMessage;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "ReplicaResponse{" +
                "replicaId=" + replicaId +
                ", signature=" + signature +
                '}';
    }
}
