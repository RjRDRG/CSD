package com.csd.proxy.impl;

import bftsmart.tom.ServiceProxy;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.request.IRequest;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ErrorResponse;
import com.csd.common.response.wrapper.OkResponse;
import com.csd.common.response.wrapper.Response;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.csd.proxy.db.TransactionEntity;
import com.csd.proxy.db.TransactionRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.*;

@Component
public class LedgerProxy extends ServiceProxy {

    private final TransactionRepository transactionsRepository;

    public LedgerProxy(Environment environment, TransactionRepository transactionsRepository) {
        super(environment.getProperty("proxy.id", Integer.class));
        this.transactionsRepository = transactionsRepository;
    }

    @SuppressWarnings("unchecked")
    public <R extends IRequest, T extends Serializable> Response<T> invokeUnordered(R request) {
        try {
            ConsensusRequest consensusRequest = new ConsensusRequest(request, 0);

            byte[] reply = super.invokeUnordered(dataToBytes(consensusRequest));
            if(reply.length == 0)
                return new ErrorResponse<>(Status.NOT_AVAILABLE, "Not enough correct replicas");

            ConsensusResponse consensusResponse = bytesToData(reply);

            transactionsRepository.saveAll(Arrays.stream(consensusResponse.getMissingEntries()).map(TransactionEntity::new).collect(Collectors.toList()));

            Result<T> result = consensusResponse.extractResult();
            if(result.valid())
                return new OkResponse<>(result.value());
            else
                return new ErrorResponse<>(result);
        } catch (Exception e) {
            return new ErrorResponse<>(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    @SuppressWarnings("unchecked")
    public <R extends IRequest, T extends Serializable> Response<T> invokeOrdered(R request) {
        try {
            ConsensusRequest consensusRequest = new ConsensusRequest(request, 0);

            byte[] reply = super.invokeOrdered(dataToBytes(consensusRequest));
            if(reply.length == 0)
                return new ErrorResponse<>(Status.NOT_AVAILABLE, "Not enough correct replicas");

            ConsensusResponse consensusResponse = bytesToData(reply);

            transactionsRepository.saveAll(Arrays.stream(consensusResponse.getMissingEntries()).map(TransactionEntity::new).collect(Collectors.toList()));

            Result<T> result = consensusResponse.extractResult();
            if(result.valid())
                return new OkResponse<>(result.value());
            else
                return new ErrorResponse<>(result);
        } catch (Exception e) {
            return new ErrorResponse<>(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }
}

