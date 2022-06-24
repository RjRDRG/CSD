package com.csd.replica.datalayer;

import com.csd.common.item.Transaction;

import java.util.Comparator;

public class TransactionComparator implements Comparator<Transaction> {

    @Override
    public int compare(Transaction a, Transaction b) {
        return Double.compare(getFees(a), getFees(b));
    }

    public double getFees(Transaction transaction) {
        double fee = 0;
        for (Transaction.Value d : transaction.getInputs()) {
            fee = fee + d.getValue();
        }
        for (Transaction.Value d : transaction.getOutputs()) {
            fee = fee - d.getValue();
        }
        return fee;
    }

}