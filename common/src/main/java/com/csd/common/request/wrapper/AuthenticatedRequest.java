package com.csd.common.request.wrapper;

import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.IRequest;
import com.csd.common.traits.Signature;

import java.util.Arrays;

public class AuthenticatedRequest<T extends IRequest> implements IRequest {
    private byte[] clientId;
    private Signature<T> clientSignature;
    private T request;

    public AuthenticatedRequest(byte[] clientId, EncodedPublicKey clientPublicKey, SignatureSuite signatureSuite, T request) throws Exception {
        this.clientId = clientId;
        this.clientSignature = new Signature<>(clientPublicKey, signatureSuite, request);
        this.request = request;
    }

    public AuthenticatedRequest() {
    }

    public boolean verifyClientId(IDigestSuite digestSuite) throws Exception {
        return digestSuite.verify(clientSignature.getPublicKey().getEncoded(), Arrays.copyOfRange(clientId, 32, clientId.length));
    }

    public boolean verifySignature(SignatureSuite signatureSuite) throws Exception {
        return clientSignature.verify(signatureSuite, request);
    }

    public byte[] getClientId() {
        return clientId;
    }

    public void setClientId(byte[] clientId) {
        this.clientId = clientId;
    }

    public Signature<T> getClientSignature() {
        return clientSignature;
    }

    public void setClientSignature(Signature<T> clientSignature) {
        this.clientSignature = clientSignature;
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
