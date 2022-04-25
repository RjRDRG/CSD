package com.csd.replica.impl;

import com.csd.replica.db.TransactionEntity;

import java.io.Serializable;
import java.util.List;

public class Snapshot implements Serializable {
    private List<TransactionEntity> transactions;

    public Snapshot(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }
}