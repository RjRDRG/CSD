package com.csd.replica.consensuslayer.pow;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.item.Resource;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.csd.replica.servicelayer.ReplicaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;

import static com.csd.common.util.Serialization.bytesToData;
import static com.csd.common.util.Serialization.dataToBytes;

@Component
public class PowReplica extends DefaultSingleRecoverable {

    private static final Logger log = LoggerFactory.getLogger(PowReplica.class);

    private int replicaId;
    private final ReplicaService replicaService;
    private final Environment environment;

    private final RequestValidator validator;

    public PowReplica(ReplicaService replicaService, Environment environment) throws Exception {
        super();
        this.replicaService = replicaService;
        this.environment = environment;
        this.validator = new RequestValidator(environment.getProperty("proxy.quorum.size" , int.class));
    }

    public void start(String[] args) {
        replicaId = args.length > 0 ? Integer.parseInt(args[0]) : environment.getProperty("replica.id", int.class);
        log.info("The id of the replica is: " + replicaId);
        new ServiceReplica(replicaId, this, this);
    }

    public ConsensusResponse execute(ConsensusRequest consensusRequest) {
        switch (consensusRequest.getType()) {
            case BALANCE: {
                GetBalanceRequestBody request =  consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastTrxDate(request.getClientId()[0]), false);
                Result<Double> result =  v.valid() ? replicaService.getBalance(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case LOAD: {
                LoadMoneyRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastTrxDate(request.getClientId()[0]), false);
                Result<LoadMoneyRequestBody> result =  v.valid() ? replicaService.loadMoney(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case TRANSFER: {
                SendTransactionRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastTrxDate(request.getClientId()[0]), true);
                Result<SendTransactionRequestBody> result =  v.valid() ? replicaService.sendTransaction(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case EXTRACT: {
                GetExtractRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastTrxDate(request.getClientId()[0]), false);
                Result<Resource[]> result = v.valid() ? replicaService.getExtract(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case TOTAL_VAL: {
                GetTotalValueRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastTrxDate(request.getClientId()[0]), false);
                Result<Double> result = v.valid() ? replicaService.getTotalValue(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case GLOBAL_VAL: {
                Result<Double> result = replicaService.getGlobalValue(consensusRequest.extractRequest());
                return new ConsensusResponse(result.encode(), replicaService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            case LEDGER: {
                Result<Resource[]> result = replicaService.getLedger(consensusRequest.extractRequest());
                return new ConsensusResponse(result.encode(), replicaService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
            }
            default: {
                Result<Serializable> result = Result.error(Status.NOT_IMPLEMENTED, consensusRequest.getType().name());
                return new ConsensusResponse(result.encode(), replicaService.getTransactionsAfterId(consensusRequest.getLastEntryId()));
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
            return dataToBytes(new ConsensusResponse(result.encode(), new Resource[0]));
        }
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            log.error(e.getMessage());
            Result<Serializable> result = Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
            return dataToBytes(new ConsensusResponse(result.encode(), new Resource[0]));
        }
    }

    @Override
    public byte[] getSnapshot() {
        return dataToBytes(replicaService.getSnapshot());
    }

    @Override
    public void installSnapshot(byte[] state) {
        replicaService.installSnapshot(bytesToData(state));
    }

}