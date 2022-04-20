package com.csd.common.reply;

import com.csd.common.item.Transaction;
import com.csd.common.traits.Result;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class ConsentedReply<T extends Serializable> implements Serializable {

    private long requestId;
    private Result<T> result;
    private List<Transaction> missingEntries;

    public ConsentedReply(long requestId, Result<T> result, List<Transaction> missingEntries) {
        this.requestId = requestId;
        this.result = result;
        this.missingEntries = missingEntries;
    }

    public ConsentedReply() {
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public Result<T> getResult() {
        return result;
    }

    public void setResult(Result<T> result) {
        this.result = result;
    }

    public List<Transaction> getMissingEntries() {
        return missingEntries;
    }

    public void setMissingEntries(List<Transaction> missingEntries) {
        this.missingEntries = missingEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsentedReply<?> that = (ConsentedReply<?>) o;
        return requestId == that.requestId && result.equals(that.result) && missingEntries.equals(that.missingEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, result, missingEntries);
    }

    @Override
    public String toString() {
        return "ReplicaReply{" +
                "requestId=" + requestId +
                ", result=" + result +
                ", missingEntries=" + missingEntries +
                '}';
    }
}
