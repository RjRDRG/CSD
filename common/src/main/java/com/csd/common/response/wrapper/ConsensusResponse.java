package com.csd.common.response.wrapper;

import com.csd.common.item.Transaction;
import com.csd.common.traits.Result;

import java.io.Serializable;
import java.util.Objects;

import static com.csd.common.util.Serialization.bytesToData;

public class ConsensusResponse implements Serializable {
    private Result<byte[]> encodedResult;
    private Transaction[] missingEntries;

    public <T extends Serializable> Result<T> extractReply() {
        if(encodedResult.valid())
            return Result.ok((T)bytesToData(encodedResult.value()));
        else
            return Result.error(encodedResult);
    }

    public ConsensusResponse(Result<byte[]> encodedResult, Transaction[] missingEntries) {
        this.encodedResult = encodedResult;
        this.missingEntries = missingEntries;
    }

    public ConsensusResponse() {
    }

    public Result<byte[]> getEncodedResult() {
        return encodedResult;
    }

    public void setEncodedResult(Result<byte[]> encodedResult) {
        this.encodedResult = encodedResult;
    }

    public Transaction[] getMissingEntries() {
        return missingEntries;
    }

    public void setMissingEntries(Transaction[] missingEntries) {
        this.missingEntries = missingEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensusResponse that = (ConsensusResponse) o;
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
