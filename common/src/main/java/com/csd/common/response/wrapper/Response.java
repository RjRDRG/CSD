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
    private ArrayList<ReplicaResponse> replicaResponses;
    private T response;
    private Status status;
    private String message;

    public Response(T response) {
        this.proxySignature = null;
        this.replicaResponses = new ArrayList<>();
        this.response = response;
        this.status = Status.OK;
        this.message = null;
    }

    public <E extends Serializable> Response(Result<E> result) {
        this(result.error(), result.message());
    }

    public <E extends Serializable> Response(Result<E> result, SignatureSuite signatureSuite) {
        this(result.error(), result.message());
        proxySignature(signatureSuite);
    }

    public Response(Status status, String message) {
        this.response = null;
        this.status = status;
        this.message = message;
        this.proxySignature = null;
        this.replicaResponses = new ArrayList<>();
    }

    public void proxySignature(SignatureSuite signatureSuite) {
        this.proxySignature = new Signature(
                signatureSuite,
                concat(
                        dataToBytesDeterministic(status),
                        dataToBytesDeterministic(message),
                        dataToBytesDeterministic(response),
                        dataToBytesDeterministic(replicaResponses)
                )
        );
    }

    public void replicaResponses(List<ReplicaResponse> signatures) {
        this.replicaResponses = new ArrayList<>(signatures);
    }

    public Response() {
    }

    public Signature getProxySignature() {
        return proxySignature;
    }

    public void setProxySignature(Signature proxySignature) {
        this.proxySignature = proxySignature;
    }

    public List<ReplicaResponse> getReplicaResponses() {
        return replicaResponses;
    }

    public void setReplicaResponses(ArrayList<ReplicaResponse> replicaResponses) {
        this.replicaResponses = replicaResponses;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public boolean valid() {
        return status == Status.OK;
    }

    public T response() {
        return response;
    }

    public Status error() {
        return status;
    }

    public String message() {
        return message;
    }

    public Signature proxySignature() {
        return proxySignature;
    }

    public List<ReplicaResponse> replicaResponses() {
        return replicaResponses;
    }

    @Override
    public String toString() {
        return "Response{\n" +
                "\tproxySignature=" + proxySignature +
                "\n\treplicaResponses=" + replicaResponses.stream().map(r -> "\n\t\t" + r.toString()).reduce("", (s0,s1) -> s0 + s1) +
                "\n\tstatus=" + status + ", message='" + message +
                "\n\tresponse=" + response  +
                "\n}";
    }
}
