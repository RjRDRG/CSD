package com.csd.replica.consensuslayer.bftsmart;

import com.csd.replica.datalayer.ValueEntity;

import java.io.Serializable;
import java.util.List;

public class Snapshot implements Serializable {
    private List<ValueEntity> transactions;
    private double globalValue;

    public Snapshot(List<ValueEntity> transactions, double globalValue) {
        this.transactions = transactions;
        this.globalValue = globalValue;
    }

    public List<ValueEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<ValueEntity> transactions) {
        this.transactions = transactions;
    }

    public double getGlobalValue() {
        return globalValue;
    }

    public void setGlobalValue(double globalValue) {
        this.globalValue = globalValue;
    }
}