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
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static com.csd.common.util.Serialization.*;

@Component
public class LedgerReplica extends DefaultSingleRecoverable {

    private static final Logger log = LoggerFactory.getLogger(LedgerReplica.class);

    private int replicaId;
    private final LedgerService ledgerService;
    private final Environment environment;

    private final RequestValidator validator;
    private final SessionRegistry sessions;

    public LedgerReplica(LedgerService ledgerService, Environment environment) throws Exception {
        this.ledgerService = ledgerService;
        this.environment = environment;
        this.validator = new RequestValidator();
        this.sessions = new SessionRegistry();
    }

    public void start(String[] args) {
        replicaId = args.length > 0 ? Integer.parseInt(args[0]) : environment.getProperty("replica.id", int.class);
        log.info("The id of the replica is: " + replicaId);
        new ServiceReplica(replicaId, this, this);
    }

    public ConsentedReply execute(ConsensualRequest consensualRequest) {
        switch (consensualRequest.getType()) {
            case SESSION: {
                Result<AuthenticatedRequest<StartSessionRequestBody>> request = validator.validate((AuthenticatedRequest<StartSessionRequestBody>) consensualRequest.extractRequest());
                Result<Long> result;
                String clientId = bytesToString(request.value().getClientId());
                OffsetDateTime timestamp = request.value().getRequestBody().getData().getTimestamp();
                if(!request.valid()) {
                    result = Result.error(request);
                }
                else if(timestamp.isBefore(OffsetDateTime.now().minusMinutes(10))) {
                    result = Result.error(Result.Status.BAD_REQUEST, "Session Timestamp is to old");
                }
                else if(sessions.contains(clientId)) {
                    result = Result.error(Result.Status.BAD_REQUEST, "Session already active");
                }
                else {
                    long nonce = timestamp.toInstant().toEpochMilli();
                    sessions.putSession(clientId, nonce);
                    result = Result.ok(nonce);
                }
                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
            }
            case BALANCE: {
                Result<AuthenticatedRequest<GetBalanceRequestBody>> request = validator.validate((AuthenticatedRequest<GetBalanceRequestBody>) consensualRequest.extractRequest());
                Result<Double> result = request.valid() ? ledgerService.getBalance(request.value()) : Result.error(request);
                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
            }
            case LOAD: {
                ProtectedRequest<LoadMoneyRequestBody> extractRequest = consensualRequest.extractRequest();
                String clientId = bytesToString(extractRequest.getClientId());
                Result<ProtectedRequest<LoadMoneyRequestBody>> request = validator.validate(extractRequest, sessions.getSession(clientId));
                Result<RequestInfo> result;
                if (request.valid()) {
                    result = ledgerService.loadMoney(request.value(), consensualRequest.getTimestamp());
                    sessions.increment(clientId);
                }
                else result = Result.error(request);
                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
            }
            case TRANSFER: {
                ProtectedRequest<SendTransactionRequestBody> extractRequest = consensualRequest.extractRequest();
                String clientId = bytesToString(extractRequest.getClientId());
                Result<ProtectedRequest<SendTransactionRequestBody>> request = validator.validate(extractRequest, sessions.getSession(clientId));
                Result<RequestInfo> result;
                if (request.valid()) {
                    result = ledgerService.sendTransaction(request.value(), consensualRequest.getTimestamp());
                    sessions.increment(clientId);
                }
                else result = Result.error(request);

                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
            }
            case EXTRACT: {
                Result<AuthenticatedRequest<GetExtractRequestBody>> request = validator.validate((AuthenticatedRequest<GetExtractRequestBody>) consensualRequest.extractRequest());
                Result<Transaction[]> result = request.valid() ? ledgerService.getExtract(request.value()) : Result.error(request);
                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
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
                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
            }
            case GLOBAL_VAL: {
                Result<Double> result = ledgerService.getGlobalValue(consensualRequest.extractRequest());
                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
            }
            case LEDGER: {
                Result<Transaction[]> result = ledgerService.getLedger(consensualRequest.extractRequest());
                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
            }
            default: {
                Result<Serializable> result = Result.error(Result.Status.NOT_IMPLEMENTED, consensualRequest.getType().name());
                return new ConsentedReply(result.encode(), ledgerService.getTransactionsAfterId(consensualRequest.getLastEntryId()));
            }
        }
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        try {
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            log.error(e.getMessage());
            Result<Serializable> result = Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
            return dataToBytes(new ConsentedReply(result.encode(), new Transaction[0]));
        }
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            log.error(e.getMessage());
            Result<Serializable> result = Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
            return dataToBytes(new ConsentedReply(result.encode(), new Transaction[0]));
        }
    }

    @Override
    public byte[] getSnapshot() {
        return dataToBytes(ledgerService.getSnapshot());
    }

    @Override
    public void installSnapshot(byte[] state) {
        ledgerService.installSnapshot(bytesToData(state));
    }

}