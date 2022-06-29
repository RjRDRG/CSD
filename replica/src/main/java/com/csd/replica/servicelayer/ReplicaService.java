
package com.csd.replica.servicelayer;

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
import java.util.*;

import static com.csd.common.util.Serialization.bytesToString;

@Service
public class ReplicaService {

    private static final Logger log = LoggerFactory.getLogger(ReplicaService.class);
    private final BlockHeaderRepository blockHeaderRepository;
    private final ResourceRepository resourceRepository;

    private final PriorityQueue<Transaction> transactionPoll;

    public ReplicaService(ResourceRepository resourceRepository, BlockHeaderRepository blockHeaderRepository) throws Exception {
        this.resourceRepository = resourceRepository;
        this.blockHeaderRepository = blockHeaderRepository;
        this.transactionPoll = new PriorityQueue<>(new TransactionComparator());
    }

    public Result<LoadMoneyRequestBody> loadMoney(LoadMoneyRequestBody request) {
        try {
            Transaction t = new Transaction(request.getRequestId(), null, request.getClientId()[0], request.getAmount(), 0.0, request.getNonce(), request.getClientSignature()[0].getSignature());
            transactionPoll.add(t);

            return Result.ok(request);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    @Transactional
    public Result<SendTransactionRequestBody> sendTransaction(SendTransactionRequestBody request) {
        try {
            Transaction t = new Transaction(request.getRequestId(), request.getClientId()[0], request.getRecipient(), request.getAmount(), 0.0, request.getNonce(), request.getClientSignature()[0].getSignature());
            transactionPoll.add(t);

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
                    .filter(r -> r.getType().equals(Resource.Type.VALUE.name()))
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
                        .filter(r -> r.getType().equals(Resource.Type.VALUE.name()))
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
                    .filter(r -> r.getType().equals(Resource.Type.VALUE.name()))
                    .map(ResourceEntity::getAsset)
                    .map(Double::valueOf)
                    .reduce(0.0, Double::sum);

            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public List<Transaction> getTransactionBatch(int size) {
        List<Transaction> t = new ArrayList<>(size);
        Iterator<Transaction> it = transactionPoll.iterator();
        for(int i=0; i<size; i++) {
            if(it.hasNext())
                t.add(it.next());
            else
                return t;
        }
        return t;
    }

    public void executeBlock(byte[] proposerId, BlockHeaderEntity block, List<Transaction> transactions){
        blockHeaderRepository.save(block);

        for (Transaction t : transactions) {
            transactionPoll.remove(t);
            if (t.getAsset() instanceof Double) {
                Double amount = (Double) t.getAsset();
                Double fee = (Double) t.getFee();

                if(t.getOwner() != null) {
                    ResourceEntity senderResource = new ResourceEntity(
                            t.getOwner(), Resource.Type.VALUE.name(), amount.toString(), true, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(senderResource);
                }

                if(t.getRecipient() != null) {
                    ResourceEntity recipientResource = new ResourceEntity(
                            t.getRecipient(), Resource.Type.VALUE.name(), amount.toString(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(recipientResource);
                }

                if (fee > 0) {
                    ResourceEntity feeResource = new ResourceEntity(
                            proposerId, Resource.Type.VALUE.name(), fee.toString(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(feeResource);
                }
            }
        }
    }

    public Transaction getTransaction(String txid) {
        return transactionPoll.stream().filter(t -> t.getId().equals(txid)).findAny().orElse(null);
    }

    public BlockHeaderEntity getLastBlock() {
        return blockHeaderRepository.findTopByOrderByIdDesc();
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

    public OffsetDateTime getLastTrxDate(byte[] owner) {
        return Optional.ofNullable(resourceRepository.findFirstByOwnerOrderByTimestampAsc(bytesToString(owner))).map(ResourceEntity::getTimestamp).orElse(OffsetDateTime.MIN);
    }
}