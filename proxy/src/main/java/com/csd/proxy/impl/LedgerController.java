package com.csd.proxy.impl;

import com.csd.common.item.*;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

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
}