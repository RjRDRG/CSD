package com.csd.proxy.impl;

import bftsmart.tom.ServiceProxy;
import com.csd.common.reply.ConsentedReply;
import com.csd.common.request.IRequest;
import com.csd.common.request.wrapper.ConsensualRequest;
import com.csd.common.traits.Result;
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
    public <R extends IRequest, T extends Serializable> Result<T> invokeUnordered(R request) {
        try {
            ConsensualRequest consensualRequest = new ConsensualRequest(request, 0);

            byte[] reply = super.invokeUnordered(dataToBytes(consensualRequest));
            if(reply.length == 0)
                return Result.error(Result.Status.NOT_AVAILABLE, "Not enough correct replicas");

            ConsentedReply consentedReply = bytesToData(reply);

            transactionsRepository.saveAll(Arrays.stream(consentedReply.getMissingEntries()).map(TransactionEntity::new).collect(Collectors.toList()));

            return consentedReply.extractReply();
        } catch (Exception e) {
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public <R extends IRequest, T extends Serializable> Result<T> invokeOrdered(R request) {
        try {
            ConsensualRequest consensualRequest = new ConsensualRequest(request, 0);

            byte[] reply = super.invokeOrdered(dataToBytes(consensualRequest));
            if(reply.length == 0)
                return Result.error(Result.Status.NOT_AVAILABLE, "Not enough correct replicas");

            ConsentedReply consentedReply = bytesToData(reply);

            transactionsRepository.saveAll(Arrays.stream(consentedReply.getMissingEntries()).map(TransactionEntity::new).collect(Collectors.toList()));

            return consentedReply.extractReply();
        } catch (Exception e) {
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    public long getLastTransactionId(byte[] ownerId) {
        String owner = bytesToString(ownerId);
        TransactionEntity entity = transactionsRepository.findByOwnerAndTopByOrderByIdDesc(owner);
        if (entity==null)
            return -1;
        else
            return entity.getId();
    }

}

