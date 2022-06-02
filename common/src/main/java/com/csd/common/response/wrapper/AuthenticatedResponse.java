package com.csd.common.response.wrapper;

import com.csd.common.traits.Signature;

import java.io.Serializable;
import java.util.List;

public class AuthenticatedResponse<T extends Serializable> {
    private Signature<T> proxySignature;
    private List<Signature<T>> replicaSignatures;
    private T response;

    public AuthenticatedResponse(List<Signature<T>> replicaSignatures, T response) {
        this.proxySignature = null;
        this.replicaSignatures = replicaSignatures;
        this.response = response;
    }

    public AuthenticatedResponse(AuthenticatedResponse<T> other, Signature<T> proxySignature) {
        this.proxySignature = proxySignature;
        this.replicaSignatures = other.replicaSignatures;
        this.response = other.response;
    }

    public AuthenticatedResponse() {
    }

    public Signature<T> getProxySignature() {
        return proxySignature;
    }

    public void setProxySignature(Signature<T> proxySignature) {
        this.proxySignature = proxySignature;
    }

    public List<Signature<T>> getReplicaSignatures() {
        return replicaSignatures;
    }

    public void setReplicaSignatures(List<Signature<T>> replicaSignatures) {
        this.replicaSignatures = replicaSignatures;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "AuthenticatedResponse{" +
                "proxySignature=" + proxySignature +
                ", replicaSignatures=" + replicaSignatures +
                ", response=" + response +
                '}';
    }
}