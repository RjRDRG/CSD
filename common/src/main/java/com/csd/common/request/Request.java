package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public abstract class Request implements Serializable {

    protected String requestId;

    protected HashMap<Integer,byte[]> clientId;
    protected HashMap<Integer,Signature> clientSignature;
    protected List<Signature> proxySignatures;
    protected OffsetDateTime nonce;

    public Request() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public HashMap<Integer, byte[]> getClientId() {
        return clientId;
    }

    public void setClientId(HashMap<Integer, byte[]> clientId) {
        this.clientId = clientId;
    }

    public HashMap<Integer, Signature> getClientSignature() {
        return clientSignature;
    }

    public void setClientSignature(HashMap<Integer, Signature> clientSignature) {
        this.clientSignature = clientSignature;
    }

    public List<Signature> getProxySignatures() {
        return proxySignatures;
    }

    public void setProxySignatures(List<Signature> proxySignatures) {
        this.proxySignatures = proxySignatures;
    }

    public OffsetDateTime getNonce() {
        return nonce;
    }

    public void setNonce(OffsetDateTime nonce) {
        this.nonce = nonce;
    }

    public void addProxySignature(Signature proxySignature) {
        proxySignatures.add(proxySignature);
    }

    public abstract byte[] serializedRequest();

    public byte[] serializedSignedRequest() {
        return concat(
            serializedRequest(),
            dataToBytesDeterministic(clientSignature)
        );
    }

    public boolean verifyClientSignature(IDigestSuite digestSuite, SignatureSuite signatureSuite) {
        try {
            for (int i=0; i<clientId.size(); i++) {
                byte[] id = clientId.get(i);
                Signature s = clientSignature.get(i);
                if (!digestSuite.verify(s.getPublicKey().getEncoded(), Arrays.copyOfRange(id, 32, id.length))) {
                    return false;
                }
                if(!s.verify(signatureSuite, serializedRequest(), false)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean verifyProxySignatures(List<SignatureSuite> signatureSuites, int q) {
        try {
            if(proxySignatures == null || proxySignatures.size()<q)
                return false;

            int counter = 0;

            for (SignatureSuite suite : signatureSuites) {
                for (Signature s : proxySignatures) {
                    if (suite.getPublicKey().equals(s.getPublicKey())) {
                        if (s.verify(suite, serializedSignedRequest(), true)) {
                            counter++;
                            break;
                        }
                    }
                }
            }

            return counter >= q;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean verifyNonce(OffsetDateTime other) {
        return this.nonce.isAfter(other);
    }
}
