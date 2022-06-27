package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static com.csd.common.util.Serialization.*;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class SendTransactionRequestBody extends Request {
    private byte[] recipient;
    private double amount;

    public SendTransactionRequestBody(byte[] clientId, SignatureSuite signatureSuite, byte[] recipient, double amount) {
        try {
            this.clientId = new byte[][]{clientId};
            this.clientSignature = new Signature[]{
                    new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest()))
            };
            this.nonce = OffsetDateTime.now();
            this.recipient = recipient;
            this.amount = amount;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SendTransactionRequestBody() {
    }

    public byte[] getRecipient() {
        return recipient;
    }

    public void setRecipient(byte[] recipient) {
        this.recipient = recipient;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public byte[] serializedRequest() {
        return concat(clientId[0], dataToBytesDeterministic(nonce), recipient, dataToBytesDeterministic(amount));
    }

    @Override
    public String toString() {
        return "SendTransactionRequestBody{" +
                "recipient=" + bytesToHex(recipient).substring(0,10) + ".." +
                "amount=" + amount +
                ", clientId=" + clientId[0] +
                ", clientSignature=" + clientSignature[0] +
                ", proxySignatures=" + Arrays.toString(proxySignatures) +
                ", nonce=" + nonce +
                '}';
    }
}
