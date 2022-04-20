package com.csd.common.request;

import java.io.Serializable;
import java.util.Objects;

public class GetBalanceRequestBody implements Serializable {
    private String date;

    public GetBalanceRequestBody(String date) {
        this.date = date;
    }

    public GetBalanceRequestBody() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetBalanceRequestBody that = (GetBalanceRequestBody) o;
        return date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @Override
    public String toString() {
        return "GetBalanceRequestBody{" +
                "timestamp='" + date + '\'' +
                '}';
    }
}
