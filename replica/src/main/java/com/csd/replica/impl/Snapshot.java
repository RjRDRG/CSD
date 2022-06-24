package com.csd.replica.impl;

import com.csd.replica.db.TransactionIOEntity;

import java.io.Serializable;
import java.util.List;

public class Snapshot implements Serializable {
    private List<TransactionIOEntity> transactions;
    private double globalValue;

    public Snapshot(List<TransactionIOEntity> transactions, double globalValue) {
        this.transactions = transactions;
        this.globalValue = globalValue;
    }

    public List<TransactionIOEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionIOEntity> transactions) {
        this.transactions = transactions;
    }

    public double getGlobalValue() {
        return globalValue;
    }

    public void setGlobalValue(double globalValue) {
        this.globalValue = globalValue;
    }
}