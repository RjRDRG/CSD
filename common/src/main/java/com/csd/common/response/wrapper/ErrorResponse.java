package com.csd.common.response.wrapper;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Result;
import com.csd.common.traits.Signature;
import com.csd.common.util.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class ErrorResponse<T extends Serializable> implements Response<T>{

    private Signature proxySignature;
    private ArrayList<Signature> replicaSignatures;
    private Status error;
    private String message;

    public <E extends Serializable> ErrorResponse(Result<E> result) {
        this(result.error(), result.message());
    }

    public <E extends Serializable> ErrorResponse(Result<E> result, SignatureSuite signatureSuite) {
        this(result.error(), result.message());
        proxySignature(signatureSuite);
    }

    public ErrorResponse(Status error, String message) {
        this.error = error;
        this.message = message;
        this.proxySignature = null;
        this.replicaSignatures = new ArrayList<>();
    }

    public void proxySignature(SignatureSuite signatureSuite) {
        this.proxySignature = new Signature(
                signatureSuite,
                concat(
                    concat(dataToBytesDeterministic(error), dataToBytesDeterministic(message)),
                    dataToBytesDeterministic(replicaSignatures)
                )
        );
    }

    @Override
    public void replicaSignatures(List<Signature> signatures) {
        this.replicaSignatures = new ArrayList<>(signatures);
    }

    public ErrorResponse() {
    }

    public Signature getProxySignature() {
        return proxySignature;
    }

    public void setProxySignature(Signature proxySignature) {
        this.proxySignature = proxySignature;
    }

    public ArrayList<Signature> getReplicaSignatures() {
        return replicaSignatures;
    }

    public void setReplicaSignatures(ArrayList<Signature> replicaSignatures) {
        this.replicaSignatures = replicaSignatures;
    }

    public Status getError() {
        return error;
    }

    public void setError(Status error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean valid() {
        return false;
    }

    @Override
    public T response() {
        return null;
    }

    @Override
    public Status error() {
        return error;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Signature proxySignature() {
        return proxySignature;
    }

    @Override
    public List<Signature> replicaSignatures() {
        return Collections.emptyList();
    }
}
