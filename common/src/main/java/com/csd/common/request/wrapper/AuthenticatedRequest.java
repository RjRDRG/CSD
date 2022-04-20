package com.csd.common.request.wrapper;

import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Seal;

import java.io.Serializable;

import static com.csd.common.util.Serialization.bytesToString;

public class AuthenticatedRequest<T extends Serializable> implements Serializable {
    private byte[] clientId;
    private EncodedPublicKey clientPublicKey;
    private Seal<T> requestBody;

    public AuthenticatedRequest(byte[] clientId, EncodedPublicKey clientPublicKey, Seal<T> requestBody) {
        this.clientId = clientId;
        this.clientPublicKey = clientPublicKey;
        this.requestBody = requestBody;
    }

    public boolean verifyClientId(IDigestSuite digestSuite) throws Exception {
        return digestSuite.verify(clientPublicKey.getEncoded(), clientId);
    }

    public boolean verifySignature(SignatureSuite signatureSuite) throws Exception {
        signatureSuite.setPublicKey(clientPublicKey);
        return requestBody.verify(signatureSuite);
    }

    public AuthenticatedRequest() {
    }

    public byte[] getClientId() {
        return clientId;
    }

    public void setClientId(byte[] clientId) {
        this.clientId = clientId;
    }

    public EncodedPublicKey getClientPublicKey() {
        return clientPublicKey;
    }

    public void setClientPublicKey(EncodedPublicKey clientPublicKey) {
        this.clientPublicKey = clientPublicKey;
    }

    public Seal<T> getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Seal<T> requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public String toString() {
        return "AuthenticatedRequest{" +
                "clientId=" + bytesToString(clientId) +
                ", clientPublicKey=" + clientPublicKey +
                ", requestBody=" + requestBody +
                '}';
    }
}
