
package com.csd.replica.impl;

import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.suites.digest.HashSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.item.RequestInfo;
import com.csd.common.item.Transaction;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Result;
import com.csd.replica.db.TransactionEntity;
import com.csd.replica.db.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private double globalValue;

    private final TransactionRepository transactionsRepository;

    private final IDigestSuite transactionDigestSuite;

    public LedgerService(TransactionRepository transactionsRepository) throws Exception {
        this.globalValue = 0;
        this.transactionsRepository = transactionsRepository;
        this.transactionDigestSuite = new HashSuite(new IniSpecification("transaction_digest_suite", CRYPTO_CONFIG_PATH));
    }

    public Result<RequestInfo> loadMoney(ProtectedRequest<LoadMoneyRequestBody> request, OffsetDateTime timestamp) {
        try {
            String recipientId = bytesToString(request.getClientId());
            LoadMoneyRequestBody requestBody = request.getRequestBody().getData();

            TransactionEntity t = new TransactionEntity(recipientId, requestBody.getAmount(), timestamp, getLastTransactionHash());
            transactionsRepository.save(t);

            globalValue += requestBody.getAmount();

            return Result.ok(new RequestInfo(timestamp));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    @Transactional
    public Result<RequestInfo> sendTransaction(ProtectedRequest<SendTransactionRequestBody> request, OffsetDateTime timestamp) {
        try {
            SendTransactionRequestBody requestBody = request.getRequestBody().getData();

            String senderId = bytesToString(request.getClientId());
            String recipientId = bytesToString(requestBody.getDestination());

            if (requestBody.getAmount()<0) return Result.error(Result.Status.BAD_REQUEST, "Transaction amount must be positive");

            double balance = 0;
            String clientId = bytesToString(request.getClientId());
            balance += transactionsRepository.findByOwner(clientId).stream()
                    .map(TransactionEntity::getAmount)
                    .reduce(0.0, Double::sum);

            if (balance < requestBody.getAmount()) return Result.error(Result.Status.BAD_REQUEST, "Insufficient Credit");

            TransactionEntity sender = new TransactionEntity(senderId, -requestBody.getAmount(), timestamp, getLastTransactionHash());
            TransactionEntity recipient = new TransactionEntity(recipientId, requestBody.getAmount(), timestamp, getTransactionHash(sender.toItem()));

            transactionsRepository.save(sender);
            transactionsRepository.save(recipient);

            return Result.ok(new RequestInfo(timestamp));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Transaction[]> getExtract(AuthenticatedRequest<GetExtractRequestBody> request) {
        try {
            GetExtractRequestBody requestBody = request.getRequestBody().getData();

            String ownerId = bytesToString(request.getClientId());

            return Result.ok(transactionsRepository.findByOwner(ownerId).stream()
                    .map(TransactionEntity::toItem).toArray(Transaction[]::new));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getTotalValue(GetTotalValueRequestBody request) {
        try {
            double acm = 0;
            for(AuthenticatedRequest<IRequest.Void> authenticatedRequest : request.getListOfAccounts()) {
                String clientId = bytesToString(authenticatedRequest.getClientId());
                acm += transactionsRepository.findByOwner(clientId).stream()
                        .map(TransactionEntity::getAmount)
                        .reduce(0.0, Double::sum);
            }
            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Transaction[]> getLedger(GetLedgerRequestBody request) {
        try {
            return Result.ok(transactionsRepository.findAll().stream().map(TransactionEntity::toItem).toArray(Transaction[]::new));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getGlobalValue(GetGlobalValueRequestBody request) {
        try {
            return Result.ok(globalValue);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<Double> getBalance(AuthenticatedRequest<GetBalanceRequestBody> request) {
        try {
            GetBalanceRequestBody requestBody = request.getRequestBody().getData();
            double acm = 0;

            String clientId = bytesToString(request.getClientId());
            acm += transactionsRepository.findByOwner(clientId).stream()
                    .map(TransactionEntity::getAmount)
                    .reduce(0.0, Double::sum);

            return Result.ok(acm);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Result.Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public void installSnapshot(Snapshot snapshot) {
        transactionsRepository.deleteAll();
        transactionsRepository.saveAll(snapshot.getTransactions());
        globalValue = snapshot.getGlobalValue();
    }

    public Snapshot getSnapshot() {
        return new Snapshot(transactionsRepository.findAll(), globalValue);
    }

    public Transaction[] getTransactionsAfterId(long id) {
        return transactionsRepository.findByIdGreaterThan(id).stream().map(TransactionEntity::toItem).toArray(Transaction[]::new);
    }

    private String getLastTransactionHash() throws Exception {
        TransactionEntity entity = transactionsRepository.findTopByOrderByIdDesc();
        if (entity==null)
            return "";
        else
            return getTransactionHash(entity.toItem());
    }

    private String getTransactionHash(Transaction transaction) throws Exception {
        return bytesToString(transactionDigestSuite.digest(dataToBytesDeterministic(transaction)));
    }
}