package com.csd.common.request.wrapper;

import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.IRequest;
import com.csd.common.traits.Signature;

import java.util.Arrays;

public class SignedRequest<T extends IRequest> implements IRequest {
    private byte[] id;
    private Signature<T> signature;
    private T request;

    public SignedRequest(byte[] id, SignatureSuite signatureSuite, T request) throws Exception {
        this.id = id;
        this.signature = new Signature<>(signatureSuite.getPublicKey(), signatureSuite, request);
        this.request = request;
    }

    public SignedRequest() {
    }

    public boolean verifyId(IDigestSuite digestSuite) throws Exception {
        return digestSuite.verify(signature.getPublicKey().getEncoded(), Arrays.copyOfRange(id, 32, id.length));
    }

    public boolean verifySignature(SignatureSuite signatureSuite) throws Exception {
        return signature.verify(signatureSuite, request);
    }

    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public Signature<T> getSignature() {
        return signature;
    }

    public void setSignature(Signature<T> signature) {
        this.signature = signature;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }

    @Override
    public Type type() {
        return request.type();
    }
}
