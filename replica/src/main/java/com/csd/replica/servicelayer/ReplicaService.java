
package com.csd.replica.servicelayer;

import com.csd.common.cryptography.hlib.HomoAdd;
import com.csd.common.datastructs.Poll;
import com.csd.common.item.PrivateValueAsset;
import com.csd.common.item.Resource;
import com.csd.common.item.ValueToken;
import com.csd.common.request.*;
import com.csd.common.traits.Result;
import com.csd.common.traits.Signature;
import com.csd.common.util.Format;
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
    private final ResourceRepository resourceRepository;

    private final Poll<Transaction> transactionPoll;

    public ReplicaService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
        this.transactionPoll = new Poll<>(new TransactionComparator());
    }

    public Result<LoadMoneyRequestBody> loadMoney(LoadMoneyRequestBody request) {
        try {
            Transaction t = new Transaction(request.getRequestId(), Transaction.Type.Value, null, request.getClientId().get(0), request.getAmount(), 0.0, request.getNonce(), request.getClientSignature().get(0).getSignature());
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
                        Transaction.Type.PrivateValue,
                        request.getClientId().get(0),
                        request.getRecipient(),
                        new PrivateValueAsset(request.getRequestId(), request.getEncryptedAmount(), request.getAmount()),
                        request.getFee(),
                        request.getNonce(),
                        request.getClientSignature().values().stream().map(Signature::getSignature).reduce(new byte[0], Serialization::concat)
                );
            } else {
                t = new Transaction(
                        request.getRequestId(),
                        Transaction.Type.Value,
                        request.getClientId().get(0),
                        request.getRecipient(),
                        request.getAmount(),
                        request.getFee(),
                        request.getNonce(),
                        request.getClientSignature().values().stream().map(Signature::getSignature).reduce(new byte[0], Serialization::concat)
                );
            }
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

            String clientId = bytesToString(request.getClientId().get(0));
            acm += resourceRepository.findByOwner(clientId).stream()
                    .filter(r -> r.getType().equals(Resource.Type.VALUE.name()))
                    .map(r -> Double.parseDouble(r.getAsset()) * (r.isSpent() ? -1 : 1) )
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

            List<ResourceEntity> rs = resourceRepository.findByOwner(clientId).stream()
                    .filter(r -> r.getType().equals(Resource.Type.CRYPT.name()))
                    .collect(Collectors.toList());

            if(rs.isEmpty()) {
                return Result.error(Status.NOT_FOUND, "No encrypted value");
            }

            List<BigInteger> l = rs.stream().collect(Collectors.groupingBy(ResourceEntity::getAsset))
                    .values().stream()
                    .filter(resourceEntities -> resourceEntities.size() == 1)
                    .map(resourceEntities -> new BigInteger(extractEncryptedAmount(resourceEntities.get(0).getAsset())))
                    .collect(Collectors.toList());

            if(l.isEmpty()) {
                return Result.error(Status.NOT_FOUND, "No encrypted value");
            }

            BigInteger result = l.get(0);
            for (int i = 1; i < l.size(); i++) {
                result = HomoAdd.sum(result, l.get(i), nSquare);
            }

            return Result.ok(result.toByteArray());
        } catch (Exception e) {
            log.error(Format.exception(e));
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
                    Transaction.Type.Claim,
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
                        .map(r -> Double.parseDouble(r.getAsset()) * (r.isSpent() ? -1 : 1) )
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
                    .map(r -> Double.parseDouble(r.getAsset()) * (r.isSpent() ? -1 : 1) )
                    .reduce(0.0, Double::sum);

            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public List<Transaction> getTransactionBatch(int size) {
        return transactionPoll.getN(size);
    }

    public void executeBlock(byte[] proposerId, BlockHeaderEntity block, List<Transaction> transactions){
        log.info("Executing block: " + block.getTimestamp());

        for (Transaction t : transactions) {
            transactionPoll.remove(t);
            if (t.getType().equals(Transaction.Type.Value)) {
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
                            block.getId(), t.getOwner(), Resource.Type.VALUE.name(), fee.toString(), true, t.getTimestamp(), t.getRequestSignature()
                    );
                    ResourceEntity proposerResource = new ResourceEntity(
                            block.getId(), proposerId, Resource.Type.VALUE.name(), fee.toString(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(feeResource);
                    resourceRepository.save(proposerResource);
                }
            }
            else if(t.getType() == Transaction.Type.PrivateValue) {
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
                            block.getId(), t.getOwner(), Resource.Type.VALUE.name(), fee.toString(), true, t.getTimestamp(), t.getRequestSignature()
                    );
                    ResourceEntity proposerResource = new ResourceEntity(
                            block.getId(), proposerId, Resource.Type.VALUE.name(), fee.toString(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(feeResource);
                    resourceRepository.save(proposerResource);
                }
            }
            else if(t.getType() == Transaction.Type.Claim) {
                ValueToken valueToken = (ValueToken) t.getAsset();
                Double fee = (Double) t.getFee();

                if(t.getRecipient() != null) {
                    ResourceEntity tokenResource = new ResourceEntity(
                            block.getId(), t.getRecipient(), Resource.Type.CRYPT.name(), valueToken.getPrivateValueAsset().getAsset(), true, t.getTimestamp(), t.getRequestSignature()
                    );
                    ResourceEntity valueResource = new ResourceEntity(
                            block.getId(), t.getRecipient(), Resource.Type.VALUE.name(), ""+valueToken.getPrivateValueAsset().getAmount(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(tokenResource);
                    resourceRepository.save(valueResource);
                }

                if (fee > 0) {
                    ResourceEntity feeResource = new ResourceEntity(
                            block.getId(), t.getOwner(), Resource.Type.VALUE.name(), fee.toString(), true, t.getTimestamp(), t.getRequestSignature()
                    );
                    ResourceEntity proposerResource = new ResourceEntity(
                            block.getId(), proposerId, Resource.Type.VALUE.name(), fee.toString(), false, t.getTimestamp(), t.getRequestSignature()
                    );
                    resourceRepository.save(feeResource);
                    resourceRepository.save(proposerResource);
                }
            }
        }
    }

    public Transaction getTransaction(String txid) {
        return transactionPoll.getElement(t -> t.getId().equals(txid));
    }

    public void installSnapshot(Snapshot snapshot) {
        resourceRepository.deleteAll();
        resourceRepository.saveAll(snapshot.getResources());
    }

    public Snapshot getSnapshot() {
        return new Snapshot(resourceRepository.findAll());
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