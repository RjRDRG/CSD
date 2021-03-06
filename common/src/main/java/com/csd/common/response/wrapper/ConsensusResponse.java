package com.csd.common.response.wrapper;

import com.csd.common.item.Resource;
import com.csd.common.traits.Result;

import java.io.Serializable;

import static com.csd.common.util.Serialization.bytesToData;

public class ConsensusResponse implements Serializable {
    private Result<byte[]> encodedResult;
    private Resource[] missingEntries;

    @SuppressWarnings("unchecked")
    public <T extends Serializable> Result<T> extractResult() {
        if(encodedResult.valid())
            return Result.ok((T)bytesToData(encodedResult.value()));
        else
            return Result.error(encodedResult);
    }

    public ConsensusResponse(Result<byte[]> encodedResult, Resource[] missingEntries) {
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

    public Resource[] getMissingEntries() {
        return missingEntries;
    }

    public void setMissingEntries(Resource[] missingEntries) {
        this.missingEntries = missingEntries;
    }

    @Override
    public String toString() {
        return "ReplicaReply{" +
                ", encodedResult=" + encodedResult +
                ", missingEntries=" + missingEntries +
                '}';
    }
}
