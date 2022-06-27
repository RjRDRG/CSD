package com.csd.replica.datalayer;

import java.util.Comparator;

public class TransactionComparator implements Comparator<Transaction> {

    @Override
    public int compare(Transaction a, Transaction b) {
        if(a.getFee() instanceof Double && b.getFee() instanceof Double)
            return Double.compare((Double) a.getFee(), (Double) b.getFee());
        else
            return 0;
    }
}