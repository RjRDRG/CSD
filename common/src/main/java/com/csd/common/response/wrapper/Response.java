package com.csd.common.response.wrapper;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Result;
import com.csd.common.traits.Signature;
import com.csd.common.util.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class Response<T extends Serializable> {

    private Signature proxySignature;
    private ArrayList<Signature> replicaSignatures;
    private T response;
    private Status error;
    private String message;

    public Response(T response) {
        this.proxySignature = null;
        this.replicaSignatures = new ArrayList<>();
        this.response = response;
        this.error = Status.OK;
        this.message = null;
    }

    public <E extends Serializable> Response(Result<E> result) {
        this(result.error(), result.message());
    }

    public <E extends Serializable> Response(Result<E> result, SignatureSuite signatureSuite) {
        this(result.error(), result.message());
        proxySignature(signatureSuite);
    }

    public Response(Status error, String message) {
        this.response = null;
        this.error = error;
        this.message = message;
        this.proxySignature = null;
        this.replicaSignatures = new ArrayList<>();
    }

    public void proxySignature(SignatureSuite signatureSuite) {
        this.proxySignature = new Signature(
                signatureSuite,
                concat(
                        dataToBytesDeterministic(error),
                        dataToBytesDeterministic(message),
                        dataToBytesDeterministic(response),
                        dataToBytesDeterministic(replicaSignatures)
                )
        );
    }

    public void replicaSignatures(List<Signature> signatures) {
        this.replicaSignatures = new ArrayList<>(signatures);
    }

    public Response() {
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

    public boolean valid() {
        return error == Status.OK;
    }

    public T response() {
        return response;
    }

    public Status error() {
        return error;
    }

    public String message() {
        return message;
    }

    public Signature proxySignature() {
        return proxySignature;
    }

    public List<Signature> replicaSignatures() {
        return replicaSignatures;
    }

    @Override
    public String toString() {
        return "Response{" +
                "proxySignature=" + (proxySignature==null) +
                ", replicaSignatures=" + replicaSignatures.size() +
                ", response=" + response +
                ", error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}
