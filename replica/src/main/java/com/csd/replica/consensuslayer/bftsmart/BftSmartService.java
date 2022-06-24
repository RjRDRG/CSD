
package com.csd.replica.consensuslayer.bftsmart;

import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.suites.digest.HashSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.item.Transaction;
import com.csd.common.item.TransactionDetails;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.SignedRequest;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.csd.replica.datalayer.BlockHeaderRepository;
import com.csd.replica.datalayer.TransactionComparator;
import com.csd.replica.datalayer.ValueEntity;
import com.csd.replica.datalayer.ValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.PriorityQueue;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

@Service
public class BftSmartService {

    private static final Logger log = LoggerFactory.getLogger(BftSmartService.class);

    private double globalValue;

    private final IDigestSuite blockDigestSuite;
    private final BlockHeaderRepository blockHeaderRepository;
    private final ValueRepository valueRepository;
    private final PriorityQueue<Transaction> openTransactions;

    public BftSmartService(ValueRepository valueRepository, BlockHeaderRepository blockHeaderRepository) throws Exception {
        this.globalValue = 0;
        this.valueRepository = valueRepository;
        this.blockHeaderRepository = blockHeaderRepository;
        this.blockDigestSuite = new HashSuite(new IniSpecification("block_digest_suite", CRYPTO_CONFIG_PATH));
        this.openTransactions = new PriorityQueue<>(new TransactionComparator());
    }

    public Result<TransactionDetails> loadMoney(SignedRequest<LoadMoneyRequestBody> request) {
        try {
            String recipientId = bytesToString(request.getId());
            LoadMoneyRequestBody requestBody = request.getRequest();

            ValueEntity t = new ValueEntity(recipientId, requestBody.getAmount(), request.getNonce(), getLastTransactionHash());
            valueRepository.save(t);

            globalValue += requestBody.getAmount();

            return Result.ok(new TransactionDetails(request.getNonce()));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    @Transactional
    public Result<TransactionDetails> sendTransaction(SignedRequest<SendTransactionRequestBody> request) {
        try {
            SendTransactionRequestBody requestBody = request.getRequest();

            String senderId = bytesToString(request.getId());
            String recipientId = bytesToString(requestBody.getDestination());

            if (requestBody.getAmount()<0) return Result.error(Status.BAD_REQUEST, "Transaction amount must be positive");

            double balance = 0;
            String clientId = bytesToString(request.getId());
            balance += valueRepository.findByOwner(clientId).stream()
                    .map(ValueEntity::getAmount)
                    .reduce(0.0, Double::sum);

            if (balance < requestBody.getAmount()) return Result.error(Status.BAD_REQUEST, "Insufficient Credit");

            ValueEntity sender = new ValueEntity(senderId, -requestBody.getAmount(), request.getNonce(), getLastTransactionHash());
            ValueEntity recipient = new ValueEntity(recipientId, requestBody.getAmount(), request.getNonce(), getTransactionHash(sender.toItem()));

            valueRepository.save(sender);
            valueRepository.save(recipient);

            return Result.ok(new TransactionDetails(request.getNonce()));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Transaction[]> getExtract(SignedRequest<GetExtractRequestBody> request) {
        try {
            GetExtractRequestBody requestBody = request.getRequest();

            String ownerId = bytesToString(request.getId());

            return Result.ok(valueRepository.findByOwner(ownerId).stream()
                    .map(ValueEntity::toItem).toArray(Transaction[]::new));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getTotalValue(GetTotalValueRequestBody request) {
        try {
            double acm = 0;
            for(SignedRequest<Request.Void> signedRequest : request.getListOfAccounts()) {
                String clientId = bytesToString(signedRequest.getId());
                acm += valueRepository.findByOwner(clientId).stream()
                        .map(ValueEntity::getAmount)
                        .reduce(0.0, Double::sum);
            }
            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Transaction[]> getLedger(GetLedgerRequestBody request) {
        try {
            return Result.ok(valueRepository.findAll().stream().map(ValueEntity::toItem).toArray(Transaction[]::new));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getGlobalValue(GetGlobalValueRequestBody request) {
        try {
            return Result.ok(globalValue);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getBalance(SignedRequest<GetBalanceRequestBody> request) {
        try {
            GetBalanceRequestBody requestBody = request.getRequest();
            double acm = 0;

            String clientId = bytesToString(request.getId());
            acm += valueRepository.findByOwner(clientId).stream()
                    .map(ValueEntity::getAmount)
                    .reduce(0.0, Double::sum);

            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public void installSnapshot(Snapshot snapshot) {
        valueRepository.deleteAll();
        valueRepository.saveAll(snapshot.getTransactions());
        globalValue = snapshot.getGlobalValue();
    }

    public Snapshot getSnapshot() {
        return new Snapshot(valueRepository.findAll(), globalValue);
    }

    public Transaction[] getTransactionsAfterId(long id) {
        return valueRepository.findByIdGreaterThan(id).stream().map(ValueEntity::toItem).toArray(Transaction[]::new);
    }

    private String getLastTransactionHash() throws Exception {
        ValueEntity entity = valueRepository.findTopByOrderByIdDesc();
        if (entity==null)
            return "";
        else
            return getTransactionHash(entity.toItem());
    }

    private String getTransactionHash(Transaction transaction) throws Exception {
        return bytesToString(blockDigestSuite.digest(dataToBytesDeterministic(transaction)));
    }

    public OffsetDateTime getLastTrxDate(byte[] owner) {
        return Optional.ofNullable(valueRepository.findFirstByOwnerOrderByTimestampAsc(bytesToString(owner))).map(ValueEntity::getTimestamp).orElse(OffsetDateTime.MIN);
    }
}