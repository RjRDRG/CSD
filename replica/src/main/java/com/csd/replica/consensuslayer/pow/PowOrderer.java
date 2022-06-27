package com.csd.replica.consensuslayer.pow;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.suites.digest.HashSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.datastructs.MerkleTree;
import com.csd.common.item.Resource;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.traits.Result;
import com.csd.common.util.Serialization;
import com.csd.common.util.Status;
import com.csd.replica.consensuslayer.IConsensusLayer;
import com.csd.replica.datalayer.Block;
import com.csd.replica.datalayer.BlockHeaderEntity;
import com.csd.replica.datalayer.Transaction;
import com.csd.replica.servicelayer.ReplicaService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.common.util.Serialization.*;

@Component
public class PowOrderer extends DefaultSingleRecoverable implements IConsensusLayer {

    private static final Logger log = LoggerFactory.getLogger(PowOrderer.class);
    private int replicaId;
    private final ReplicaService replicaService;
    private final Environment environment;

    private final RequestValidator validator;

    private ServiceReplica serviceReplica;

    public PowOrderer(ReplicaService replicaService, Environment environment) throws Exception {
        super();
        this.replicaService = replicaService;
        this.environment = environment;
        this.validator = new RequestValidator(environment.getProperty("proxy.quorum.size" , int.class));
    }

    public void start(String[] args) throws Exception {
        replicaId = args.length > 0 ? Integer.parseInt(args[0]) : environment.getProperty("replica.id", int.class);
        log.info("The id of the replica is: " + replicaId);
        serviceReplica = new ServiceReplica(replicaId, this, this);
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