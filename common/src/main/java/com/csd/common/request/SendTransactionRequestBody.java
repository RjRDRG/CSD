package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;
import com.csd.common.util.Serialization;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static com.csd.common.util.Serialization.*;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class SendTransactionRequestBody extends Request {
    private byte[] recipient;
    private double amount;

    private double fee;

    private byte[] encryptedAmount;

    public SendTransactionRequestBody(byte[] clientId, SignatureSuite signatureSuite, byte[] recipient, double amount, double fee) {
        try {
            this.clientId = new byte[][]{clientId};
            this.nonce = OffsetDateTime.now();
            this.recipient = recipient;
            this.amount = amount;
            this.fee = fee;
            this.encryptedAmount = null;
            this.proxySignatures = new Signature[0];
            this.clientSignature = new Signature[]{
                    new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest()))
            };
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void encrypt(byte[] clientId, SignatureSuite signatureSuite, byte[] encryptedAmount) {
        try {
            this.clientId = new byte[][]{this.clientId[0], clientId};
            this.encryptedAmount = encryptedAmount;
            this.clientSignature = new Signature[]{
                    clientSignature[0],
                    new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest()))
            };
        }catch (Exception e) {
            e.printStackTrace();
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

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public byte[] getEncryptedAmount() {
        return encryptedAmount;
    }

    public void setEncryptedAmount(byte[] encryptedAmount) {
        this.encryptedAmount = encryptedAmount;
    }

    @Override
    public byte[] serializedRequest() {
        return concat(dataToBytesDeterministic(nonce), recipient, dataToBytesDeterministic(amount), dataToBytesDeterministic(fee));
    }

    @Override
    public String toString() {
        return "SendTransactionRequestBody{" +
                "recipient=" + bytesToHex(recipient).substring(0,10) + ".." +
                "amount=" + amount +
                "fee=" + fee +
                "encryptedAmount=" + bytesToHex(encryptedAmount) +
                ", clientId=" + Arrays.stream(clientId).map(Serialization::bytesToHex).reduce("" , (s1, s2) -> s1+", "+s2) +
                ", clientSignature=" + Arrays.stream(clientSignature).map(Signature::getSignature).map(Serialization::bytesToHex).reduce("" , (s1, s2) -> s1+", "+s2) +
                ", proxySignatures=" + Arrays.stream(proxySignatures).map(Signature::getSignature).map(Serialization::bytesToHex).reduce("" , (s1, s2) -> s1+", "+s2) +
                ", nonce=" + nonce +
                '}';
    }
}
