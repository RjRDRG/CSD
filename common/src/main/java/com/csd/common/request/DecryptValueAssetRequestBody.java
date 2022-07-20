package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.item.ValueToken;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class DecryptValueAssetRequestBody extends Request {

    private ValueToken token;
    private double fee;

    public DecryptValueAssetRequestBody(byte[] clientId, SignatureSuite signatureSuite, ValueToken token, double fee) {
        this.fee = fee;
        try {
            this.requestId = UUID.randomUUID().toString();
            this.clientId = new HashMap<>();
            this.clientId.put(0,clientId);
            this.nonce = OffsetDateTime.now();
            this.token = token;
            this.clientSignature = new HashMap<>();
            this.clientSignature.put(0, new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest())));

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DecryptValueAssetRequestBody() {
    }

    public ValueToken getToken() {
        return token;
    }

    public void setToken(ValueToken token) {
        this.token = token;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    @Override
    public byte[] serializedRequest() {
        return concat(dataToBytesDeterministic(requestId), clientId.get(0), dataToBytesDeterministic(nonce), dataToBytesDeterministic(token), dataToBytesDeterministic(fee));
    }

    @Override
    public String toString() {
        return "DecryptValueAssetRequestBody{" +
                "clientId=" + clientId.get(0) +
                ", clientSignature=" + clientSignature.get(0) +
                ", proxySignatures=" + proxySignatures +
                ", nonce=" + nonce +
                ", token=" + token +
                ", fee=" + fee +
                '}';
    }
}
