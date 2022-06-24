package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class LoadMoneyRequestBody extends Request {
    private double amount;

    public LoadMoneyRequestBody(byte[] clientId, SignatureSuite signatureSuite, double amount) {
        try {
            this.clientId = new byte[][]{clientId};
            this.clientSignature = new Signature[]{
                    new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest()))
            };
            this.nonce = OffsetDateTime.now();
            this.amount = amount;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LoadMoneyRequestBody() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public byte[] serializedRequest() {
        return concat(clientId[0], dataToBytesDeterministic(nonce), dataToBytesDeterministic(amount));
    }

    @Override
    public String toString() {
        return "LoadMoneyRequestBody{" +
                "amount=" + amount +
                ", clientId=" + clientId[0] +
                ", clientSignature=" + clientSignature[0] +
                ", proxySignatures=" + Arrays.toString(proxySignatures) +
                ", nonce=" + nonce +
                '}';
    }
}
