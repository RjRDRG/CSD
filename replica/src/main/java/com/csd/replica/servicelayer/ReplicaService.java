
package com.csd.replica.servicelayer;

import com.csd.common.cryptography.hlib.HomoAdd;
import com.csd.common.item.PrivateValueAsset;
import com.csd.common.item.Resource;
import com.csd.common.item.ValueToken;
import com.csd.common.request.*;
import com.csd.common.response.wrapper.ReplicaResponse;
import com.csd.common.traits.Result;
import com.csd.common.traits.Signature;
import com.csd.common.util.Serialization;
import com.csd.common.util.Status;
import com.csd.replica.datalayer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.csd.common.item.PrivateValueAsset.extractEncryptedAmount;
import static com.csd.common.util.Serialization.*;

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
            Transaction t = new Transaction(request.getRequestId(), null, request.getClientId().get(0), request.getAmount(), 0.0, request.getNonce(), request.getClientSignature().get(0).getSignature());
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
            Transaction t;
            if(request.getEncryptedAmount() != null) {
                t = new Transaction(
                        request.getRequestId(),
                        request.getClientId().get(0),
                        request.getRecipient(),
                        new PrivateValueAsset(request.getRequestId(), request.getEncryptedAmount(), request.getAmount()),
                        request.getFee(),
                        request.getNonce(),
                        request.getClientSignature().values().stream().map(Signature::getSignature).reduce(new byte[0], Serialization::concat)
                );
                transactionPoll.add(t);
                return Result.ok(request);
            } else {
                t = new Transaction(
                        request.getRequestId(),
                        request.getClientId().get(0),
                        request.getRecipient(),
                        request.getAmount(),
                        request.getFee(),
                        request.getNonce(),
                        request.getClientSignature().values().stream().map(Signature::getSignature).reduce(new byte[0], Serialization::concat)
                );
                transactionPoll.add(t);
                return Result.ok(request);
            }
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getBalance(GetBalanceRequestBody request) {
        try {
            double acm = 0;

            String clientId = bytesToString(request.getClientId().get(0));
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

    public Result<byte[]> getEncryptedBalance(GetEncryptedBalanceRequestBody request) {
        try {
            String clientId = bytesToString(request.getClientId().get(0));

            BigInteger nSquare = new BigInteger(request.getnSquare());

            List<BigInteger> l = resourceRepository.findByOwner(clientId).stream()
                    .filter(r -> r.getType().equals(Resource.Type.CRYPT.name()))
                    .collect(Collectors.groupingBy(ResourceEntity::getAsset))
                    .values().stream()
                    .filter(resourceEntities -> resourceEntities.size() == 1)
                    .map(resourceEntities -> new BigInteger(extractEncryptedAmount(resourceEntities.get(0).getAsset())))
                    .collect(Collectors.toList());

            BigInteger result = l.get(0);
            for (int i = 1; i < l.size(); i++) {
                result = HomoAdd.sum(result, l.get(i), nSquare);
            }

            return Result.ok(result.toByteArray());
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> decryptValueAsset(DecryptValueAssetRequestBody request) {
        try {
            List<ResourceEntity> resourceEntity = resourceRepository.findByAsset(request.getToken().getPrivateValueAsset().getAsset());

            if(resourceEntity.size() == 0)
                return Result.error(Status.NOT_FOUND, "Resource not found");

            if(resourceEntity.size() > 1)
                return Result.error(Status.CONFLICT, "Resource already spent");

            if(!resourceEntity.get(0).getOwner().equals(bytesToString(request.getClientId().get(0))))
                return Result.error(Status.FORBIDDEN, "The asset belongs to someone else");

            if(!validateTokenAmountAndSignatures(request.getToken()))
                return Result.error(Status.FORBIDDEN, "Invalid token");

            Transaction t = new Transaction(
                    request.getRequestId(),
                    request.getClientId().get(0),
                    request.getClientId().get(0),
                    request.getToken(),
                    request.getFee(),
                    request.getNonce(),
                    request.getClientSignature().values().stream().map(Signature::getSignature).reduce(new byte[0], Serialization::concat)
            );

            transactionPoll.add(t);

            return Result.ok(request.getToken().getPrivateValueAsset().getAmount());
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public boolean validateTokenAmountAndSignatures(ValueToken token){

/*
        Map<Double,Integer> tokenAmountEndorsements = new HashMap<>();
        for (ReplicaResponse response : token.getReplicaResponsesAndSignatures()) {

            if(!response.getSignature().verify(replicaSignatureSuite,response.getSerializedMessage(),true))
                continue;

            SendTransactionRequestBody s = response.getSerializedMessage();
            TODO: Can't deserialize this message

            tokenAmountEndorsements.merge(s.getAmount(), 1, Integer::sum);
        }

        Map.Entry<Double,Integer> endorsedAmount = tokenAmountEndorsements.entrySet().stream().min(Map.Entry.comparingByValue());
        Double tokenAmount = token.getPrivateValueAsset.getAmount();

        if(endorsedAmount.getKey() != tokenAmount || tokenAmount.getValue()<quorum)
            return false;
*/

        return true;
    }

    public Result<Resource[]> getExtract(GetExtractRequestBody request) {
        try {
            String clientId = bytesToString(request.getClientId().get(0));

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
            for(byte[] c : request.getClientId().values()) {
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
                            block.getId(), t.getOwner(), Resource.Type.VALUE.name(), amount.toString(), true, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(senderResource);
                }

                if(t.getRecipient() != null) {
                    ResourceEntity recipientResource = new ResourceEntity(
                            block.getId(), t.getRecipient(), Resource.Type.VALUE.name(), amount.toString(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(recipientResource);
                }

                if (fee > 0) {
                    ResourceEntity feeResource = new ResourceEntity(
                            block.getId(), proposerId, Resource.Type.VALUE.name(), fee.toString(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(feeResource);
                }
            }
            else if(t.getAsset() instanceof PrivateValueAsset) {
                PrivateValueAsset privateValueAsset = (PrivateValueAsset) t.getAsset();
                Double fee = (Double) t.getFee();

                if(t.getOwner() != null) {
                    ResourceEntity senderResource = new ResourceEntity(
                            block.getId(), t.getOwner(), Resource.Type.VALUE.name(), ""+privateValueAsset.getAmount(), true, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(senderResource);
                }

                if(t.getRecipient() != null) {
                    ResourceEntity recipientResource = new ResourceEntity(
                            block.getId(), t.getRecipient(), Resource.Type.CRYPT.name(), privateValueAsset.getAsset(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(recipientResource);
                }

                if (fee > 0) {
                    ResourceEntity feeResource = new ResourceEntity(
                            block.getId(), proposerId, Resource.Type.VALUE.name(), fee.toString(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(feeResource);
                }
            }
            else if(t.getAsset() instanceof ValueToken) {
                ValueToken valueToken = (ValueToken) t.getAsset();
                Double fee = (Double) t.getFee();

                if(t.getOwner() != null) {
                    ResourceEntity senderResource = new ResourceEntity(
                            block.getId(), t.getRecipient(), Resource.Type.CRYPT.name(), valueToken.getPrivateValueAsset().getAsset(), true, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(senderResource);
                }

                if(t.getRecipient() != null) {
                    ResourceEntity recipientResource = new ResourceEntity(
                            block.getId(), proposerId, Resource.Type.VALUE.name(), ""+valueToken.getPrivateValueAsset().getAmount(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(recipientResource);
                }

                if (fee > 0) {
                    ResourceEntity feeResource = new ResourceEntity(
                            block.getId(), proposerId, Resource.Type.VALUE.name(), fee.toString(), false, t.getTimestamp(), t.getRequestSignature()
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

    public Resource[] getResourcesAfterId(long id) {
        if(id < 0)
            return new Resource[0];
        else
            return resourceRepository.findByIdGreaterThan(id).stream().map(ResourceEntity::toItem).toArray(Resource[]::new);
    }

    public OffsetDateTime getLastResourceDate(byte[] owner) {
        return Optional.ofNullable(resourceRepository.findFirstByOwnerOrderByTimestampAsc(bytesToString(owner))).map(ResourceEntity::getTimestamp).orElse(OffsetDateTime.MIN);
    }
}