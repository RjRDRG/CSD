package com.csd.common.request;

public class LoadMoneyRequestBody implements IRequest {
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

    @Override
    public IRequest.Type type() {
        return IRequest.Type.LOAD;
    }
}
