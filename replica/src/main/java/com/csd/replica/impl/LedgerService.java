
package com.csd.replica.impl;

import com.csd.common.item.RequestInfo;
import com.csd.common.request.GetBalanceRequestBody;
import com.csd.common.request.LoadMoneyRequestBody;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    public LedgerService() {
    }

    public Result<RequestInfo> loadMoney(ProtectedRequest<LoadMoneyRequestBody> request, OffsetDateTime timestamp) {
        return Result.ok(new RequestInfo(timestamp));
    }

    public Result<Double> getBalance(AuthenticatedRequest<GetBalanceRequestBody> request) {
        return Result.ok(14.0);
    }
}