package com.csd.common.traits;

import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.SignatureSuite;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.csd.common.util.Serialization.dataToJson;

public class Signature {
    private EncodedPublicKey publicKey;
    private byte[] signature;

    public Signature(SignatureSuite signatureSuite, byte[] data) {
        this.publicKey = signatureSuite.getPublicKey();
        try {
            this.signature = signatureSuite.digest(dataToJson(data).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Signature() {
    }

    public boolean verify(SignatureSuite signatureSuite, byte[] data) throws Exception {
        signatureSuite.setPublicKey(publicKey);
        return signatureSuite.verify(data, signature);
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