package com.csd.common.request;

import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.bytesToString;

public class SendTransactionRequestBody implements IRequest {
    private byte[] destination;
    private double value;

    public SendTransactionRequestBody(byte[] destination, int value) {
        this.destination = destination;
        this.value = value;
    }

    public byte[] getDestination() {
        return destination;
    }

    public void setDestination(byte[] destination) {
        this.destination = destination;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendTransactionRequestBody that = (SendTransactionRequestBody) o;
        return value == that.value && Arrays.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(value);
        result = 31 * result + Arrays.hashCode(destination);
        return result;
    }

    @Override
    public String toString() {
        return "SendTransactionRequestBody{" +
                "destination=" + bytesToString(destination) +
                ", value=" + value +
                '}';
    }

    @Override
    public Type type() {
        return Type.TRANSFER;
    }
}
