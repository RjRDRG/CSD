package com.csd.common.reply;

import com.csd.common.item.Transaction;
import com.csd.common.traits.Result;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static com.csd.common.util.Serialization.bytesToData;

public class ConsentedReply implements Serializable {
    private Result<byte[]> encodedResult;
    private List<Transaction> missingEntries;

    public <T extends Serializable> Result<T> extractReply() {
        if(encodedResult.isOK())
            return Result.ok((T)bytesToData(encodedResult.value()));
        else
            return Result.error(encodedResult.error());
    }

    public ConsentedReply(Result<byte[]> encodedResult, List<Transaction> missingEntries) {
        this.encodedResult = encodedResult;
        this.missingEntries = missingEntries;
    }

    public ConsentedReply() {
    }

    public Result<byte[]> getEncodedResult() {
        return encodedResult;
    }

    public void setEncodedResult(Result<byte[]> encodedResult) {
        this.encodedResult = encodedResult;
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
        ConsentedReply that = (ConsentedReply) o;
        return encodedResult.equals(that.encodedResult) && missingEntries.equals(that.missingEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodedResult, missingEntries);
    }

    @Override
    public String toString() {
        return "ReplicaReply{" +
                ", encodedResult=" + encodedResult +
                ", missingEntries=" + missingEntries +
                '}';
    }
}
