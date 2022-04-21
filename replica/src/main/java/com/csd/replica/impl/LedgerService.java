
package com.csd.replica.impl;

import com.csd.common.item.RequestInfo;
import com.csd.common.item.Transaction;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    public LedgerService() {
    }

    public Result<RequestInfo> loadMoney(ProtectedRequest<LoadMoneyRequestBody> request, OffsetDateTime timestamp) {
        return Result.ok(new RequestInfo(timestamp));
    }

    public Result<RequestInfo> sendTransaction(ProtectedRequest<SendTransactionRequestBody> request, OffsetDateTime timestamp) {
        return Result.ok(new RequestInfo(timestamp));
    }

    public Result<ArrayList<Transaction>> getExtract(AuthenticatedRequest<GetExtractRequestBody> request) {
        return Result.ok(new ArrayList<>());
    }

    public Result<Double> getTotalValue(GetTotalValueRequestBody request) {
        return Result.ok(10.0);
    }

    public Result<ArrayList<Transaction>> getLedger(GetLedgerRequestBody request) {
        return Result.ok(new ArrayList<>());
    }

    public Result<Double> getGlobalValue(GetGlobalValueRequestBody request) {
        return Result.ok(10.0);
    }

    public Result<Double> getBalance(AuthenticatedRequest<GetBalanceRequestBody> request) {
        return Result.ok(14.0);
    }
}