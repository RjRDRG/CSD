package com.csd.common.response.wrapper;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;
import com.csd.common.util.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class OkResponse<T extends Serializable> implements Response<T> {

    private Signature proxySignature;
    private ArrayList<Signature> replicaSignatures;
    private T response;

    public OkResponse(T response) {
        this.proxySignature = null;
        this.replicaSignatures = new ArrayList<>();
        this.response = response;
    }

    public void proxySignature(SignatureSuite signatureSuite) {
        this.proxySignature = new Signature(signatureSuite, concat(dataToBytesDeterministic(response), dataToBytesDeterministic(replicaSignatures)));
    }

    @Override
    public void replicaSignatures(List<Signature> signatures) {
        this.replicaSignatures = new ArrayList<>(signatures);
    }

    public OkResponse() {
    }

    public Signature getProxySignature() {
        return proxySignature;
    }

    public void setProxySignature(Signature proxySignature) {
        this.proxySignature = proxySignature;
    }

    public List<Signature> getReplicaSignatures() {
        return replicaSignatures;
    }

    public void setReplicaSignatures(ArrayList<Signature> replicaSignatures) {
        this.replicaSignatures = replicaSignatures;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public T response() {
        return response;
    }

    @Override
    public Status error() {
        return Status.OK;
    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public Signature proxySignature() {
        return proxySignature;
    }

    @Override
    public List<Signature> replicaSignatures() {
        return replicaSignatures;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "proxySignature=" + proxySignature +
                ", replicaSignatures=" + replicaSignatures +
                ", response=" + response +
                '}';
    }
}
