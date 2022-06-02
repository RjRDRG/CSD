package com.csd.common.request.wrapper;

import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.IRequest;
import com.csd.common.traits.Signature;
import com.csd.common.traits.UniqueSeal;

import java.util.Arrays;

import static com.csd.common.util.Serialization.bytesToString;

public class ProtectedRequest<T extends IRequest> extends AuthenticatedRequest<T> {
    private long nonce;

    public ProtectedRequest(byte[] clientId, EncodedPublicKey clientPublicKey, SignatureSuite signatureSuite, long nonce, T request) throws Exception {
        super(clientId, clientPublicKey, signatureSuite, request);
        this.nonce = nonce;
    }

    public ProtectedRequest() {
    }

    public boolean verifyNonce(long nonce) {
        return this.nonce > nonce;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "ProtectedRequest{" +
                "nonce=" + nonce +
                '}';
    }
}
