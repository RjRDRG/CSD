package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.*;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class SendTransactionRequestBody extends Request {
    private byte[] destination;
    private double amount;

    public SendTransactionRequestBody(byte[] clientId, SignatureSuite signatureSuite, byte[] destination, double amount) {
        try {
            this.clientId = new byte[][]{clientId};
            this.clientSignature = new Signature[]{
                    new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest()))
            };
            this.nonce = OffsetDateTime.now();
            this.destination = destination;
            this.amount = amount;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SendTransactionRequestBody() {
    }

    public byte[] getDestination() {
        return destination;
    }

    public void setDestination(byte[] destination) {
        this.destination = destination;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public byte[] serializedRequest() {
        return concat(clientId[0], dataToBytesDeterministic(nonce), destination, dataToBytesDeterministic(amount));
    }

    @Override
    public String toString() {
        return "SendTransactionRequestBody{" +
                "destination=" + bytesToHex(destination).substring(0,10) + ".." +
                "amount=" + amount +
                ", clientId=" + clientId[0] +
                ", clientSignature=" + clientSignature[0] +
                ", proxySignatures=" + Arrays.toString(proxySignatures) +
                ", nonce=" + nonce +
                '}';
    }
}
