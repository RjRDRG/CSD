package com.csd.replica.impl;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.Executable;
import bftsmart.tom.server.Recoverable;
import bftsmart.tom.server.Replier;
import bftsmart.tom.server.RequestVerifier;
import bftsmart.tom.server.defaultservices.DefaultReplier;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.util.KeyLoader;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.item.TransactionDetails;
import com.csd.common.item.Transaction;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.SignedRequest;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;

import static com.csd.common.util.Serialization.*;

@Component
public class LedgerReplica extends DefaultSingleRecoverable {

    private static final Logger log = LoggerFactory.getLogger(LedgerReplica.class);

    private int replicaId;
    private final LedgerService ledgerService;
    private final Environment environment;

    private final RequestValidator validator;

    public LedgerReplica(LedgerService ledgerService, Environment environment) throws Exception {
        super();
        this.ledgerService = ledgerService;
        this.environment = environment;
        this.validator = new RequestValidator();
    }

    public void start(String[] args) {
        replicaId = args.length > 0 ? Integer.parseInt(args[0]) : environment.getProperty("replica.id", int.class);
        log.info("The id of the replica is: " + replicaId);
        new ServiceReplica(replicaId, this, this);
        //new ServiceReplica(replicaId, (String)"", this, this, (RequestVerifier)null, (Replier)(new DefaultReplier()), new DefaultKeyLoader());
    }

    public ConsensusResponse execute(ConsensusRequest consensusRequest) {
        switch (consensusRequest.getType()) {
            case BALANCE: {
                SignedRequest<GetBalanceRequestBody> request =  consensusRequest.extractRequest();
                var v = validator.validate(request, ledgerService.getLastTrxDate(request.getId()));
                Result<Double> result =  v.valid() ? ledgerService.getBalance(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), ledgerService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case LOAD: {
                SignedRequest<LoadMoneyRequestBody> request = consensusRequest.extractRequest();
                var v = validator.validate(request, ledgerService.getLastTrxDate(request.getId()));
                Result<TransactionDetails> result =  v.valid() ? ledgerService.loadMoney(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), ledgerService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case TRANSFER: {
                SignedRequest<SendTransactionRequestBody> request = consensusRequest.extractRequest();
                var v = validator.validate(request, ledgerService.getLastTrxDate(request.getId()));
                Result<TransactionDetails> result =  v.valid() ? ledgerService.sendTransaction(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), ledgerService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case EXTRACT: {
                SignedRequest<GetExtractRequestBody> request = consensusRequest.extractRequest();
                var v = validator.validate(request, ledgerService.getLastTrxDate(request.getId()));
                Result<Transaction[]> result = v.valid() ? ledgerService.getExtract(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), ledgerService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case TOTAL_VAL: {
                Result<GetTotalValueRequestBody> request = Result.ok(consensusRequest.extractRequest());
                for(SignedRequest<IRequest.Void> r : ((GetTotalValueRequestBody) consensusRequest.extractRequest()).getListOfAccounts()){
                    Result<SignedRequest<IRequest.Void>> result = validator.validate(r, ledgerService.getLastTrxDate(r.getId()));
                    if (!result.valid()){
                        request = Result.error(result);
                        break;
                    }
                }
                Result<Double> result = request.valid() ? ledgerService.getTotalValue(request.value()) : Result.error(request);
                return new ConsensusResponse(result.encode(), ledgerService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case GLOBAL_VAL: {
                Result<Double> result = ledgerService.getGlobalValue(consensusRequest.extractRequest());
                return new ConsensusResponse(result.encode(), ledgerService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case LEDGER: {
                Result<Transaction[]> result = ledgerService.getLedger(consensusRequest.extractRequest());
                return new ConsensusResponse(result.encode(), ledgerService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            default: {
                Result<Serializable> result = Result.error(Status.NOT_IMPLEMENTED, consensusRequest.getType().name());
                return new ConsensusResponse(result.encode(), ledgerService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
        }
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        try {
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            log.error(e.getMessage());
            Result<Serializable> result = Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
            return dataToBytes(new ConsensusResponse(result.encode(), new Transaction[0]));
        }
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            log.error(e.getMessage());
            Result<Serializable> result = Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
            return dataToBytes(new ConsensusResponse(result.encode(), new Transaction[0]));
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