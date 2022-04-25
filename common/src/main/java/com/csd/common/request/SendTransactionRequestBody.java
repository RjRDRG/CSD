package com.csd.common.request;

import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.bytesToString;

public class SendTransactionRequestBody implements IRequest {
    private byte[] destination;
    private double amount;

    public SendTransactionRequestBody(byte[] destination, double amount) {
        this.destination = destination;
        this.amount = amount;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendTransactionRequestBody that = (SendTransactionRequestBody) o;
        return amount == that.amount && Arrays.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(amount);
        result = 31 * result + Arrays.hashCode(destination);
        return result;
    }

    @Override
    public String toString() {
        return "SendTransactionRequestBody{" +
                "destination=" + bytesToString(destination) +
                ", amount=" + amount +
                '}';
    }

    @Override
    public Type type() {
        return Type.TRANSFER;
    }
}
