package com.csd.common.request;

import com.csd.common.request.wrapper.AuthenticatedRequest;

import javax.print.DocFlavor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class GetTotalValueRequestBody implements IRequest {
    private ArrayList<AuthenticatedRequest<Void>> listOfAccounts;

    public GetTotalValueRequestBody(ArrayList<AuthenticatedRequest<Void>> listOfAccounts) {
        this.listOfAccounts = listOfAccounts;
    }

    public GetTotalValueRequestBody() {
    }

    public ArrayList<AuthenticatedRequest<Void>> getListOfAccounts() {
        return listOfAccounts;
    }

    public void setListOfAccounts(ArrayList<AuthenticatedRequest<Void>> listOfAccounts) {
        this.listOfAccounts = listOfAccounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetTotalValueRequestBody that = (GetTotalValueRequestBody) o;
        return listOfAccounts.equals(that.listOfAccounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listOfAccounts);
    }

    @Override
    public String toString() {
        return "GetTotalValueRequestBody{" +
                "listOfAccounts=" + listOfAccounts +
                '}';
    }

    @Override
    public Type type() {
        return Type.TOTAL_VAL;
    }
}
