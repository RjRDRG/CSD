package com.csd.common.item;

import java.io.Serializable;

import static com.csd.common.util.Serialization.bytesToHex;
import static com.csd.common.util.Serialization.hexToBytes;

public class PrivateValueAsset implements Serializable {

    String asset;
    double amount;

    public PrivateValueAsset(String id, byte[] encryptedAmount, double amount) {
        this.asset = id + "|" + bytesToHex(encryptedAmount);
        this.amount = amount;
    }

    public static byte[] extractEncryptedAmount(String asset) {
        return hexToBytes(asset.split("\\|")[1]);
    }

    public PrivateValueAsset() {
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "PrivateValueAsset{" +
                "asset='" + asset + '\'' +
                ", amount=" + amount +
                '}';
    }
}
