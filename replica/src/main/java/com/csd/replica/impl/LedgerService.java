
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    @Transactional
    public Result<RequestInfo> sendTransaction(ProtectedRequest<SendTransactionRequestBody> request, OffsetDateTime timestamp) {
        try {
            SendTransactionRequestBody requestBody = request.getRequestBody().getData();

            String senderId = bytesToString(request.getClientId());
            String recipientId = bytesToString(requestBody.getDestination());

            if (requestBody.getAmount()<0) return Result.error(Result.Status.BAD_REQUEST, "Transaction amount must be positive");

            TransactionEntity sender = new TransactionEntity(senderId, -requestBody.getAmount(), timestamp, getLastTransactionHash());
            TransactionEntity recipient = new TransactionEntity(recipientId, requestBody.getAmount(), timestamp, getTransactionHash(sender.toItem()));

            transactionsRepository.save(sender);
            transactionsRepository.save(recipient);

            return Result.ok(new RequestInfo(timestamp));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    public Result<Transaction[]> getExtract(AuthenticatedRequest<GetExtractRequestBody> request) {
        GetExtractRequestBody requestBody = request.getRequestBody().getData();

        String ownerId = bytesToString(request.getClientId());

        return Result.ok(transactionsRepository.findByOwner(ownerId).stream()
                .map(TransactionEntity::toItem).toArray(Transaction[]::new));
    }

    public Result<Double> getTotalValue(GetTotalValueRequestBody request) {
        double acm = 0;
        for(AuthenticatedRequest<IRequest.Void> authenticatedRequest : request.getListOfAccounts()) {
            String clientId = bytesToString(authenticatedRequest.getClientId());
            acm += transactionsRepository.findByOwner(clientId).stream()
                    .map(TransactionEntity::getAmount)
                    .reduce(0.0, Double::sum);
        }
        return Result.ok(acm);
    }

    public Result<Transaction[]> getLedger(GetLedgerRequestBody request) {
        return Result.ok(transactionsRepository.findAll().stream().map(TransactionEntity::toItem).toArray(Transaction[]::new));
    }

    public Result<Double> getGlobalValue(GetGlobalValueRequestBody request) {
        return Result.ok(globalValue);
    }

    public Result<Double> getBalance(AuthenticatedRequest<GetBalanceRequestBody> request) {
        return Result.ok(14.0);
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