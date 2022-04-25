package com.csd.replica.impl;

import com.csd.replica.db.TransactionEntity;

import java.io.Serializable;
import java.util.List;

public class Snapshot implements Serializable {
    private List<TransactionEntity> transactions;
    private double globalValue;

    public Snapshot(List<TransactionEntity> transactions, double globalValue) {
        this.transactions = transactions;
        this.globalValue = globalValue;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public double getGlobalValue() {
        return globalValue;
    }

    public void setGlobalValue(double globalValue) {
        this.globalValue = globalValue;
    }
}