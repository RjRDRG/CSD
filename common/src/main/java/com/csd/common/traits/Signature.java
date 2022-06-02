package com.csd.common.traits;

import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.SignatureSuite;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.csd.common.util.Serialization.dataToJson;

public class Signature<T> {
    private EncodedPublicKey publicKey;
    private byte[] signature;

    public Signature(EncodedPublicKey publicKey, SignatureSuite signatureSuite, T data) throws Exception {
        this.publicKey = publicKey;
        this.signature = signatureSuite.digest(dataToJson(data).getBytes(StandardCharsets.UTF_8));
    }

    public Signature() {
    }

    public boolean verify(SignatureSuite signatureSuite, T data) throws Exception {
        signatureSuite.setPublicKey(publicKey);
        return signatureSuite.verify(dataToJson(data).getBytes(StandardCharsets.UTF_8), signature);
    }

    public EncodedPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(EncodedPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "Signature{" +
                "publicKey=" + publicKey +
                ", signature=" + Arrays.toString(signature) +
                '}';
    }
}
