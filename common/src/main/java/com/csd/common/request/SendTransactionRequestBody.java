package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;
import com.csd.common.util.Serialization;

import java.time.OffsetDateTime;
import java.util.*;

import static com.csd.common.util.Serialization.*;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class SendTransactionRequestBody extends Request {
    private byte[] recipient;
    private double amount;

    private double fee;

    private byte[] encryptedAmount;

    public SendTransactionRequestBody(byte[] clientId, SignatureSuite signatureSuite, byte[] recipient, double amount, double fee) {
        try {
            this.requestId = UUID.randomUUID().toString();
            this.clientId = new HashMap<>();
            this.clientId.put(0,clientId);
            this.nonce = OffsetDateTime.now();
            this.recipient = recipient;
            this.amount = amount;
            this.fee = fee;
            this.encryptedAmount = null;
            this.proxySignatures = new ArrayList<>();
            this.clientSignature = new HashMap<>();
            this.clientSignature.put(0, new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest())));
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void encrypt(byte[] clientId, SignatureSuite signatureSuite, byte[] encryptedAmount) {
        try {
            this.clientId.put(1,clientId);
            this.encryptedAmount = encryptedAmount;
            this.clientSignature.put(1, new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest())));
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
        return concat(dataToBytesDeterministic(requestId), dataToBytesDeterministic(nonce), recipient, dataToBytesDeterministic(amount), dataToBytesDeterministic(fee));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendTransactionRequestBody that = (SendTransactionRequestBody) o;
        return Double.compare(that.amount, amount) == 0 && Double.compare(that.fee, fee) == 0 && Arrays.equals(recipient, that.recipient) && Arrays.equals(encryptedAmount, that.encryptedAmount);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(amount, fee);
        result = 31 * result + Arrays.hashCode(recipient);
        result = 31 * result + Arrays.hashCode(encryptedAmount);
        return result;
    }

    @Override
    public String toString() {
        return "SendTransactionRequestBody{" +
                "recipient=" + bytesToHex(recipient).substring(0,10) + ".." +
                "amount=" + amount +
                "fee=" + fee +
                "encryptedAmount=" + bytesToHex(encryptedAmount) +
                ", clientId=" + clientId.values().stream().map(Serialization::bytesToHex).reduce("" , (s1, s2) -> s1+", "+s2) +
                ", clientSignature=" + clientSignature.values().stream().map(Signature::getSignature).map(Serialization::bytesToHex).reduce("" , (s1, s2) -> s1+", "+s2) +
                ", proxySignatures=" + proxySignatures.stream().map(Signature::getSignature).map(Serialization::bytesToHex).reduce("" , (s1, s2) -> s1+", "+s2) +
                ", nonce=" + nonce +
                '}';
    }
}
