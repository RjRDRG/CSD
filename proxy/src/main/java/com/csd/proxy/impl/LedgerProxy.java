package com.csd.proxy.impl;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import com.csd.common.request.IRequest;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ConsensusResponse;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.*;

@Component
public class LedgerProxy extends AsynchServiceProxy {

    private static final int TIMEOUT_PERIOD = 5000;
    private final TransactionRepository transactionsRepository;

    public LedgerProxy(Environment environment, TransactionRepository transactionsRepository) {
        super(environment.getProperty("proxy.id", Integer.class));
        this.transactionsRepository = transactionsRepository;
    }

    @SuppressWarnings("unchecked")
    public <R extends IRequest, T extends Serializable> Response<T> invokeUnordered(R request) {
        return invoke(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @SuppressWarnings("unchecked")
    public <R extends IRequest, T extends Serializable> Response<T> invokeOrdered(R request) {
        return invoke(request, TOMMessageType.ORDERED_REQUEST);
    }

    private <R extends IRequest, T extends Serializable> Response<T> invoke(R request, TOMMessageType type) {
        try {
            ConsensusRequest consensusRequest = new ConsensusRequest(request, 0);

            CountDownLatch latch = new CountDownLatch(1);
            LedgerReplyListener listener = new LedgerReplyListener(this, latch);
            super.invokeAsynchRequest(dataToBytes(consensusRequest), listener, type);
            latch.await(TIMEOUT_PERIOD, TimeUnit.MILLISECONDS);

            ConsensusResponse consensusResponse = listener.getResponse();
            if(consensusResponse != null) {
                transactionsRepository.saveAll(Arrays.stream(consensusResponse.getMissingEntries()).map(TransactionEntity::new).collect(Collectors.toList()));
                Result<T> result = consensusResponse.extractResult();
                Response<T> response = null;
                if(result.valid())
                    response = new OkResponse<>(result.value());
                else
                    response = new ErrorResponse<>(result);
                response.replicaSignatures(listener.getResponseSignatures());
                return response;
            }
            else {
                return new ErrorResponse<>(Status.NOT_AVAILABLE, "Not enough correct replicas");
            }
        } catch (Exception e) {
            System.out.println("\n\n\n\n");
            e.printStackTrace();
            System.out.println("\n\n\n\n");
            return new ErrorResponse<>(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }
}

