package com.csd.common.traits;

import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.suites.digest.SignatureSuite;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static com.csd.common.util.Serialization.*;

public class Signature implements Serializable {
    private EncodedPublicKey publicKey;
    private byte[] signature;

    public Signature(SignatureSuite signatureSuite, byte[] data) {
        this.publicKey = signatureSuite.getPublicKey();
        try {
            this.signature = signatureSuite.digest(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Signature() {
    }

    public Signature(EncodedPublicKey publicKey, byte[] signature) {
        this.publicKey = publicKey;
        this.signature = signature;
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
        return bytesToHex(signature).substring(0,10) + "..";
    }
}
