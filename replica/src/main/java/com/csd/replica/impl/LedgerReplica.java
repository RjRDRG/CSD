package com.csd.replica.impl;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.csd.common.item.RequestInfo;
import com.csd.common.item.Transaction;
import com.csd.common.reply.ConsentedReply;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ConsensualRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;

import static com.csd.common.util.Serialization.bytesToData;
import static com.csd.common.util.Serialization.dataToBytes;

@Component
public class LedgerReplica extends DefaultSingleRecoverable {

    private static final Logger log = LoggerFactory.getLogger(LedgerReplica.class);

    private int replicaId;
    private final LedgerService ledgerService;
    private final Environment environment;

    private final RequestValidator validator;

    public LedgerReplica(LedgerService ledgerService, Environment environment) throws Exception {
        this.ledgerService = ledgerService;
        this.environment = environment;
        this.validator = new RequestValidator();
    }

    public void start(String[] args) {
        replicaId = args.length > 0 ? Integer.parseInt(args[0]) : environment.getProperty("replica.id", int.class);
        log.info("The id of the replica is: " + replicaId);
        new ServiceReplica(replicaId, this, this);
    }

    public ConsentedReply execute(ConsensualRequest consensualRequest) {
        switch (consensualRequest.getType()) {
            case BALANCE: {
                Result<AuthenticatedRequest<GetBalanceRequestBody>> request = validator.validate((AuthenticatedRequest<GetBalanceRequestBody>) consensualRequest.extractRequest());
                Result<Double> result = request.valid() ? ledgerService.getBalance(request.value()) : Result.error(request);
                return new ConsentedReply(result.encode(), Collections.emptyList());
            }
            case LOAD: {
                Result<ProtectedRequest<LoadMoneyRequestBody>> request = validator.validate((ProtectedRequest<LoadMoneyRequestBody>) consensualRequest.extractRequest());
                Result<RequestInfo> result = request.valid() ? ledgerService.loadMoney(request.value(), consensualRequest.getTimestamp()) : Result.error(request);
                return new ConsentedReply(result.encode(), Collections.emptyList());
            }
            case TRANSFER: {
                Result<ProtectedRequest<SendTransactionRequestBody>> request = validator.validate((ProtectedRequest<SendTransactionRequestBody>) consensualRequest.extractRequest());
                Result<RequestInfo> result = request.valid() ? ledgerService.sendTransaction(request.value(), consensualRequest.getTimestamp()) : Result.error(request);
                return new ConsentedReply(result.encode(), Collections.emptyList());
            }
            case EXTRACT: {
                Result<AuthenticatedRequest<GetExtractRequestBody>> request = validator.validate((AuthenticatedRequest<GetExtractRequestBody>) consensualRequest.extractRequest());
                Result<Transaction[]> result = request.valid() ? ledgerService.getExtract(request.value()) : Result.error(request);
                return new ConsentedReply(result.encode(), Collections.emptyList());
            }
            case TOTAL_VAL: {
                Result<GetTotalValueRequestBody> request = Result.ok(consensualRequest.extractRequest());
                for( AuthenticatedRequest<IRequest.Void> authenticatedRequest : ((GetTotalValueRequestBody) consensualRequest.extractRequest()).getListOfAccounts()){
                    Result<AuthenticatedRequest<IRequest.Void>> result = validator.validate(authenticatedRequest);
                    if (! result.valid()){
                        request = Result.error(result);
                        break;
                    }
                }
                Result<Double> result = request.valid() ? ledgerService.getTotalValue(request.value()) : Result.error(request);
                return new ConsentedReply(result.encode(), Collections.emptyList());
            }
            case GLOBAL_VAL: {
                Result<Double> result = ledgerService.getTotalValue(consensualRequest.extractRequest());
                return new ConsentedReply(result.encode(), Collections.emptyList());
            }
            case LEDGER: {
                Result<Transaction[]> result = ledgerService.getLedger(consensualRequest.extractRequest());
                return new ConsentedReply(result.encode(), Collections.emptyList());
            }
            default: {
                Result<Serializable> result = Result.error(Result.Status.NOT_IMPLEMENTED, consensualRequest.getType().name());
                return new ConsentedReply(result.encode(), Collections.emptyList());
            }
        }
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        try {
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            log.error(e.getMessage());
            Result<Serializable> result = Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
            return dataToBytes(new ConsentedReply(result.encode(), Collections.emptyList()));
        }
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            log.error(e.getMessage());
            Result<Serializable> result = Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
            return dataToBytes(new ConsentedReply(result.encode(), Collections.emptyList()));
        }
    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public void installSnapshot(byte[] state) {
    }

}