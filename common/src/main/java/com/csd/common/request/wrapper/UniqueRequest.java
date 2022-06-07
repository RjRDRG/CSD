package com.csd.common.request.wrapper;

import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.IRequest;

public class UniqueRequest<T extends IRequest> extends SignedRequest<T> {
    private int nonce;

    public UniqueRequest(byte[] clientId, EncodedPublicKey clientPublicKey, SignatureSuite signatureSuite, int nonce, T request) throws Exception {
        super(clientId, clientPublicKey, signatureSuite, request);
        this.nonce = nonce;
    }

    public UniqueRequest() {
    }

    public boolean verifyNonce(long nonce) {
        return this.nonce > nonce;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "ProtectedRequest{" +
                "nonce=" + nonce +
                '}';
    }
}
