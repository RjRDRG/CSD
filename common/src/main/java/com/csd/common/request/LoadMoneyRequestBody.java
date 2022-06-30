package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class LoadMoneyRequestBody extends Request {
    private Double amount;

    public LoadMoneyRequestBody(byte[] clientId, SignatureSuite signatureSuite, Double amount) {
        try {
            this.requestId = UUID.randomUUID().toString();
            this.clientId = new HashMap<>();
            this.clientId.put(0,clientId);
            this.nonce = OffsetDateTime.now();
            this.amount = amount;
            this.clientSignature = new HashMap<>();
            this.clientSignature.put(0, new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest())));
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public LoadMoneyRequestBody() {
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public byte[] serializedRequest() {
        return concat(dataToBytesDeterministic(requestId), clientId.get(0), dataToBytesDeterministic(nonce), dataToBytesDeterministic(amount));
    }

    @Override
    public String toString() {
        return "LoadMoneyRequestBody{" +
                "amount=" + amount +
                ", clientId=" + clientId.get(0) +
                ", clientSignature=" + clientSignature.get(0) +
                ", proxySignatures=" + Arrays.toString(proxySignatures) +
                ", nonce=" + nonce +
                '}';
    }
}
