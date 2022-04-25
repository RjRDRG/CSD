package com.csd.proxy.impl;

import com.csd.common.item.*;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Result;
import com.csd.proxy.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static com.csd.proxy.exceptions.ResultExtractor.value;

@RestController
class LedgerController {
    private static final Logger log = LoggerFactory.getLogger(LedgerController.class);

    private final LedgerProxy ledgerProxy;
    private final RequestValidator validator;

    LedgerController(LedgerProxy ledgerProxy) throws Exception {
        this.ledgerProxy = ledgerProxy;
        this.validator = new RequestValidator();
    }

    @PostMapping("/session")
    public Long startSession(@RequestBody AuthenticatedRequest<StartSessionRequestBody> request) {
        if(request.getRequestBody().getData().getTimestamp().isBefore(OffsetDateTime.now().minusMinutes(10)))
            throw new BadRequestException("Session Timestamp is to old");
        Result<Long> result = ledgerProxy.invokeOrdered(value(validator.validate(request)));
        value(result);
        return result.value();
    }

    @PostMapping("/load")
    public RequestInfo loadMoney(@RequestBody ProtectedRequest<LoadMoneyRequestBody> request) {
        Result<RequestInfo> result = ledgerProxy.invokeOrdered(value(validator.validate(request)));
        value(result);
        return result.value();
    }

    @PostMapping("/balance")
    public Double getBalance(@RequestBody AuthenticatedRequest<GetBalanceRequestBody> request) {
        Result<Double> result = ledgerProxy.invokeUnordered(value(validator.validate(request)));
        value(result);
        return result.value();
    }

    @PostMapping("/transfer")
    public RequestInfo sendTransaction(@RequestBody ProtectedRequest<SendTransactionRequestBody> request) {
        Result<RequestInfo> result = ledgerProxy.invokeOrdered(value(validator.validate(request)));
        value(result);
        return result.value();
    }

    @PostMapping("/extract")
    public Transaction[] getExtract(@RequestBody AuthenticatedRequest<GetExtractRequestBody> request) {
        Result<Transaction[]> result = ledgerProxy.invokeOrdered(value(validator.validate(request)));
        value(result);
        return result.value();
    }

    @PostMapping("/total")
    public Double getTotalValue(@RequestBody GetTotalValueRequestBody request) {
        for( AuthenticatedRequest<IRequest.Void> authenticatedRequest : request.getListOfAccounts()){
            value(validator.validate(authenticatedRequest));
        }
        Result<Double> result = ledgerProxy.invokeOrdered(request);
        value(result);
        return result.value();
    }

    @PostMapping("/global")
    public Double getGlobalValue(@RequestBody GetGlobalValueRequestBody request) {
        Result<Double> result = ledgerProxy.invokeOrdered(request);
        value(result);
        return result.value();
    }

    @PostMapping("/ledger")
    public Transaction[] getLedger(@RequestBody GetLedgerRequestBody request) {
        Result<Transaction[]> result = ledgerProxy.invokeOrdered(request);
        value(result);
        return result.value();
    }
}