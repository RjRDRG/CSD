package com.csd.common.request;

import java.io.Serializable;

public class LoadMoneyRequestBody implements Serializable {
    private double amount;

    public LoadMoneyRequestBody(double amount) {
        this.amount = amount;
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
    public String toString() {
        return "LoadMoneyRequestBody{" +
                "amount=" + amount +
                '}';
    }
}
