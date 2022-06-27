
package com.csd.replica.servicelayer;

import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.suites.digest.HashSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.item.Resource;
import com.csd.common.request.*;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.csd.replica.datalayer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

@Service
public class ReplicaService {

    private static final Logger log = LoggerFactory.getLogger(ReplicaService.class);
    private final BlockHeaderRepository blockHeaderRepository;
    private final ResourceRepository resourceRepository;

    private final PriorityQueue<Transaction> transactionPoll;
    private final IDigestSuite blockDigestSuite;

    public ReplicaService(ResourceRepository resourceRepository, BlockHeaderRepository blockHeaderRepository) throws Exception {
        this.resourceRepository = resourceRepository;
        this.blockHeaderRepository = blockHeaderRepository;
        this.transactionPoll = new PriorityQueue<>(new TransactionComparator());
        this.blockDigestSuite = new HashSuite(new IniSpecification("block_digest_suite", CRYPTO_CONFIG_PATH));
    }

    public Result<LoadMoneyRequestBody> loadMoney(LoadMoneyRequestBody request) {
        try {
            Transaction t = new Transaction(null, request.getClientId()[0], request.getAmount(), 0.0, request.getNonce(), request.getClientSignature()[0].getSignature());
            transactionPoll.add(t);

            request.setRequestId(UUID.randomUUID().toString());

            return Result.ok(request);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    @Transactional
    public Result<SendTransactionRequestBody> sendTransaction(SendTransactionRequestBody request) {
        try {
            Transaction t = new Transaction(request.getClientId()[0], request.getRecipient(), request.getAmount(), 0.0, request.getNonce(), request.getClientSignature()[0].getSignature());
            transactionPoll.add(t);

            request.setRequestId(UUID.randomUUID().toString());

            return Result.ok(request);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getBalance(GetBalanceRequestBody request) {
        try {
            double acm = 0;

            String clientId = bytesToString(request.getClientId()[0]);
            acm += resourceRepository.findByOwner(clientId).stream()
                    .map(ResourceEntity::getAsset)
                    .map(Double::valueOf)
                    .reduce(0.0, Double::sum);

            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Resource[]> getExtract(GetExtractRequestBody request) {
        try {
            String clientId = bytesToString(request.getClientId()[0]);

            return Result.ok(resourceRepository.findByOwner(clientId).stream()
                    .map(ResourceEntity::toItem).toArray(Resource[]::new));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getTotalValue(GetTotalValueRequestBody request) {
        try {
            double acm = 0;
            for(byte[] c : request.getClientId()) {
                String clientId = bytesToString(c);
                acm += resourceRepository.findByOwner(clientId).stream()
                        .map(ResourceEntity::getAsset)
                        .map(Double::valueOf)
                        .reduce(0.0, Double::sum);
            }
            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Resource[]> getLedger(GetLedgerRequestBody request) {
        try {
            return Result.ok(resourceRepository.findAll().stream().map(ResourceEntity::toItem).toArray(Resource[]::new));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getGlobalValue(GetGlobalValueRequestBody request) {
        try {
            double acm = 0;

            acm += resourceRepository.findAll().stream()
                    .map(ResourceEntity::getAsset)
                    .map(Double::valueOf)
                    .reduce(0.0, Double::sum);

            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public void installSnapshot(Snapshot snapshot) {
        resourceRepository.deleteAll();
        resourceRepository.saveAll(snapshot.getResources());
        blockHeaderRepository.deleteAll();
        blockHeaderRepository.saveAll(snapshot.getBlocks());
    }

    public Snapshot getSnapshot() {
        return new Snapshot(blockHeaderRepository.findAll(), resourceRepository.findAll());
    }

    public Resource[] getTransactionsAfterId(long id) {
        return resourceRepository.findByIdGreaterThan(id).stream().map(ResourceEntity::toItem).toArray(Resource[]::new);
    }

    private String getLastTransactionHash() throws Exception {
        ResourceEntity entity = resourceRepository.findTopByOrderByIdDesc();
        if (entity==null)
            return "";
        else
            return getTransactionHash(entity.toItem());
    }

    private String getTransactionHash(Resource resource) throws Exception {
        return bytesToString(blockDigestSuite.digest(dataToBytesDeterministic(resource)));
    }

    public OffsetDateTime getLastTrxDate(byte[] owner) {
        return Optional.ofNullable(resourceRepository.findFirstByOwnerOrderByTimestampAsc(bytesToString(owner))).map(ResourceEntity::getTimestamp).orElse(OffsetDateTime.MIN);
    }
}