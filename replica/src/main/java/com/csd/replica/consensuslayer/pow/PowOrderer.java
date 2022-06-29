package com.csd.replica.consensuslayer.pow;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.suites.digest.HashSuite;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.datastructs.MerkleTree;
import com.csd.common.item.Resource;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.traits.Result;
import com.csd.common.util.Serialization;
import com.csd.common.util.Status;
import com.csd.replica.datalayer.Block;
import com.csd.replica.datalayer.BlockHeaderEntity;
import com.csd.replica.datalayer.Transaction;
import com.csd.replica.servicelayer.ReplicaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.common.util.Serialization.*;

@Component
public class PowOrderer extends DefaultSingleRecoverable {

    private static final Logger log = LoggerFactory.getLogger(PowOrderer.class);
    private int replicaId;
    private final ReplicaService replicaService;
    private final Environment environment;

    private final RequestValidator validator;

    private ServiceReplica serviceReplica;

    private MinerThread minerThread;

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
        this.minerThread = new MinerThread(
                environment.getProperty("replica.id" , int.class) + 100000,
                replicaService,
                new HashSuite(new IniSpecification("transaction_digest_suite", CRYPTO_CONFIG_PATH)),
                new HashSuite(new IniSpecification("block_digest_suite", CRYPTO_CONFIG_PATH)),
                environment.getProperty("replica.block.size" , int.class),
                environment.getProperty("replica.difficulty.target" , int.class),
                environment.getProperty("replica.block.reward" , Double.class)
        );
        minerThread.start();
    }

    public ConsensusResponse execute(ConsensusRequest consensusRequest) {
        switch (consensusRequest.getType()) {
            case BALANCE: {
                GetBalanceRequestBody request =  consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), false);
                Result<Double> result =  v.valid() ? replicaService.getBalance(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case HIDDEN: {
                GetEncryptedBalanceRequestBody request =  consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), false);
                Result<byte[]> result =  v.valid() ? replicaService.getEncryptedBalance(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case DECRYPT: {
                DecryptValueAssetRequestBody request =  consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), false);
                Result<Double> result =  v.valid() ? replicaService.decryptValueAsset(request) : Result.error(v);

                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case LOAD: {
                LoadMoneyRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), false);
                Result<LoadMoneyRequestBody> result =  v.valid() ? replicaService.loadMoney(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case TRANSFER: {
                SendTransactionRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), true);
                Result<SendTransactionRequestBody> result =  v.valid() ? replicaService.sendTransaction(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case EXTRACT: {
                GetExtractRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), false);
                Result<Resource[]> result = v.valid() ? replicaService.getExtract(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case TOTAL_VAL: {
                GetTotalValueRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), false);
                Result<Double> result = v.valid() ? replicaService.getTotalValue(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case GLOBAL_VAL: {
                Result<Double> result = replicaService.getGlobalValue(consensusRequest.extractRequest());
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case LEDGER: {
                Result<Resource[]> result = replicaService.getLedger(consensusRequest.extractRequest());
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            case BLOCK: {
                BlockProposal request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), false);
                Result<Long> result =  v.valid() ? blockProposalHandler(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), null);
            }
            default: {
                Result<Serializable> result = Result.error(Status.NOT_IMPLEMENTED, consensusRequest.getType().name());
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
        }
    }

    public Result<Long> blockProposalHandler(BlockProposal request) {
        try {
            Block block = request.getBlock();
            BlockHeaderEntity lastBlock = replicaService.getLastBlock();

            if(block.getDifficultyTarget() != minerThread.getDifficultyTarget())
                return Result.error(Status.BAD_REQUEST, "Invalid block");

            if(block.getPreviousBlockHash() != lastBlock.getHash())
                return Result.error(Status.CONFLICT, "Block already mined");

            Set<String> transactionIds = new HashSet<>(block.getTXIDs());

            if(transactionIds.size() != block.getTXIDs().size())
                return Result.error(Status.BAD_REQUEST, "Duplicated transactions in block");

            List<Transaction> transactions = new ArrayList<>();
            for(String txid : block.getTXIDs()) {
                Transaction t = replicaService.getTransaction(txid);
                if(t == null && !txid.equals(request.getCoinbase().getId()))
                    return Result.error(Status.CONFLICT, "Transaction already mined");
                transactions.add(t);
            }

            if(!(request.getCoinbase().getAsset() instanceof Double) || request.getCoinbase().getAsset() != minerThread.getBlockReward())
                return Result.error(Status.BAD_REQUEST, "Invalid block");

            transactions.add(request.getCoinbase());

            byte[] merkleRoot =  new MerkleTree(
                    transactions.stream().map(Serialization::dataToBytesDeterministic).collect(Collectors.toList()),
                    minerThread.getTransactionDigestSuite()
            ).getRoot().sig;

            if(merkleRoot != block.getMerkleRootHash())
                return Result.error(Status.BAD_REQUEST, "Invalid block");

            byte[] blockHash = minerThread.getBlockDigestSuite().digest(block.serializedBlock());
            if(!bytesToHex(blockHash).startsWith(StringUtils.repeat('0', block.getDifficultyTarget())))
                return Result.error(Status.BAD_REQUEST, "Invalid block");

            BlockHeaderEntity newBlock = new BlockHeaderEntity(
                    block.getTimestamp(),
                    block.getPreviousBlockHash(),
                    block.getMerkleRootHash(),
                    block.getDifficultyTarget(),
                    block.getProof()
            );

            newBlock.setHash(blockHash);

            replicaService.executeBlock(request.getClientId()[0], newBlock, transactions);

            minerThread.restart();

            return Result.ok(newBlock.getId());
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
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