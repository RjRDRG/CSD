
package com.csd.replica.impl;

import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.suites.digest.HashSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.item.TransactionDetails;
import com.csd.common.item.Transaction;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.SignedRequest;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.csd.replica.db.BlockHeaderRepository;
import com.csd.replica.db.TransactionIOEntity;
import com.csd.replica.db.TransactionIORepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private double globalValue;

    private final BlockHeaderRepository blockHeaderRepository;
    private final TransactionIORepository transactionIORepository;

    private final IDigestSuite transactionDigestSuite;

    private final 

    public LedgerService(TransactionIORepository transactionIORepository, BlockHeaderRepository blockHeaderRepository) throws Exception {
        this.globalValue = 0;
        this.transactionIORepository = transactionIORepository;
        this.blockHeaderRepository = blockHeaderRepository;
        this.transactionDigestSuite = new HashSuite(new IniSpecification("transaction_digest_suite", CRYPTO_CONFIG_PATH));
    }

    public Result<TransactionDetails> loadMoney(SignedRequest<LoadMoneyRequestBody> request) {
        try {
            String recipientId = bytesToString(request.getId());
            LoadMoneyRequestBody requestBody = request.getRequest();

            TransactionIOEntity t = new TransactionIOEntity(recipientId, requestBody.getAmount(), request.getNonce(), getLastTransactionHash());
            transactionIORepository.save(t);

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
            balance += transactionIORepository.findByOwner(clientId).stream()
                    .map(TransactionIOEntity::getAmount)
                    .reduce(0.0, Double::sum);

            if (balance < requestBody.getAmount()) return Result.error(Status.BAD_REQUEST, "Insufficient Credit");

            TransactionIOEntity sender = new TransactionIOEntity(senderId, -requestBody.getAmount(), request.getNonce(), getLastTransactionHash());
            TransactionIOEntity recipient = new TransactionIOEntity(recipientId, requestBody.getAmount(), request.getNonce(), getTransactionHash(sender.toItem()));

            transactionIORepository.save(sender);
            transactionIORepository.save(recipient);

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

            return Result.ok(transactionIORepository.findByOwner(ownerId).stream()
                    .map(TransactionIOEntity::toItem).toArray(Transaction[]::new));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getTotalValue(GetTotalValueRequestBody request) {
        try {
            double acm = 0;
            for(SignedRequest<IRequest.Void> signedRequest : request.getListOfAccounts()) {
                String clientId = bytesToString(signedRequest.getId());
                acm += transactionIORepository.findByOwner(clientId).stream()
                        .map(TransactionIOEntity::getAmount)
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
            return Result.ok(transactionIORepository.findAll().stream().map(TransactionIOEntity::toItem).toArray(Transaction[]::new));
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
            acm += transactionIORepository.findByOwner(clientId).stream()
                    .map(TransactionIOEntity::getAmount)
                    .reduce(0.0, Double::sum);

            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public void installSnapshot(Snapshot snapshot) {
        transactionIORepository.deleteAll();
        transactionIORepository.saveAll(snapshot.getTransactions());
        globalValue = snapshot.getGlobalValue();
    }

    public Snapshot getSnapshot() {
        return new Snapshot(transactionIORepository.findAll(), globalValue);
    }

    public Transaction[] getTransactionsAfterId(long id) {
        return transactionIORepository.findByIdGreaterThan(id).stream().map(TransactionIOEntity::toItem).toArray(Transaction[]::new);
    }

    private String getLastTransactionHash() throws Exception {
        TransactionIOEntity entity = transactionIORepository.findTopByOrderByIdDesc();
        if (entity==null)
            return "";
        else
            return getTransactionHash(entity.toItem());
    }

    private String getTransactionHash(Transaction transaction) throws Exception {
        return bytesToString(transactionDigestSuite.digest(dataToBytesDeterministic(transaction)));
    }

    public OffsetDateTime getLastTrxDate(byte[] owner) {
        return Optional.ofNullable(transactionIORepository.findFirstByOwnerOrderByTimestampAsc(bytesToString(owner))).map(TransactionIOEntity::getTimestamp).orElse(OffsetDateTime.MIN);
    }
}