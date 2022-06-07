package com.csd.proxy.impl;

import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.item.*;
import com.csd.common.response.ProposedMinedBlockResponse;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.SignedRequest;
import com.csd.common.request.wrapper.UniqueRequest;
import com.csd.common.response.wrapper.ErrorResponse;
import com.csd.common.response.wrapper.Response;
import com.csd.common.traits.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.proxy.exceptions.ResponseEntityBuilder.buildResponse;

@RestController
class LedgerController {
    private static final Logger log = LoggerFactory.getLogger(LedgerController.class);

    private final LedgerProxy ledgerProxy;
    private final RequestValidator validator;
    private final SignatureSuite proxySignatureSuite;

    LedgerController(LedgerProxy ledgerProxy) throws Exception {
        this.ledgerProxy = ledgerProxy;
        this.validator = new RequestValidator();
        this.proxySignatureSuite = new SignatureSuite(new IniSpecification("proxy_signature_suite", CRYPTO_CONFIG_PATH));
    }

    @PostMapping("/session")
    public ResponseEntity<Response<Integer>> startSession(@RequestBody SignedRequest<StartSessionRequestBody> request) {
        var v = validator.validate(request);
        if(!v.valid()) {
            return buildResponse(new ErrorResponse<>(v, proxySignatureSuite));
        }

        Response<Integer> response = ledgerProxy.invokeUnordered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/load")
    public ResponseEntity<Response<TransactionDetails>> loadMoney(@RequestBody UniqueRequest<LoadMoneyRequestBody> request) {
        var v = validator.validate(request);
        if(!v.valid()) {
            return buildResponse(new ErrorResponse<>(v, proxySignatureSuite));
        }

        Response<TransactionDetails> response = ledgerProxy.invokeOrdered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/balance")
    public ResponseEntity<Response<Double>> getBalance(@RequestBody SignedRequest<GetBalanceRequestBody> request) {
        var v = validator.validate(request);
        if(!v.valid()) {
            return buildResponse(new ErrorResponse<>(v, proxySignatureSuite));
        }

        Response<Double> response = ledgerProxy.invokeUnordered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Response<TransactionDetails>> sendTransaction(@RequestBody UniqueRequest<SendTransactionRequestBody> request) {
        var v = validator.validate(request);
        if(!v.valid()) {
            return buildResponse(new ErrorResponse<>(v, proxySignatureSuite));
        }

        Response<TransactionDetails> response = ledgerProxy.invokeOrdered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/extract")
    public ResponseEntity<Response<Transaction[]>> getExtract(@RequestBody SignedRequest<GetExtractRequestBody> request) {
        MultiSignedResponse<Result<Transaction[]>> result = ledgerProxy.invokeUnordered(map(validator.validate(request)));
        map(result);
        return result.value();
    }

    @PostMapping("/total")
    public ResponseEntity<Response<Double>> getTotalValue(@RequestBody GetTotalValueRequestBody request) {
        for( SignedRequest<IRequest.Void> signedRequest : request.getListOfAccounts()){
            map(validator.validate(signedRequest));
        }
        MultiSignedResponse<Result<Double>> result = ledgerProxy.invokeUnordered(request);
        map(result);
        return result.value();
    }

    @PostMapping("/global")
    public ResponseEntity<Response<Double>> getGlobalValue(@RequestBody GetGlobalValueRequestBody request) {
        MultiSignedResponse<Result<Double>> result = ledgerProxy.invokeUnordered(request);
        map(result);
        return result.value();
    }

    @PostMapping("/ledger")
    public ResponseEntity<Response<Transaction[]>> getLedger(@RequestBody GetLedgerRequestBody request) {
        MultiSignedResponse<Result<Transaction[]>> result = ledgerProxy.invokeUnordered(request);
        map(result);
        return result.value();
    }

    @PostMapping("/ledger")
    public ResponseEntity<Response<Block>> getBlockToMine(@RequestBody GetBlockToMineRequestBody request) {
        Response<Block> response = ledgerProxy.invokeUnordered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/ledger")
    public ResponseEntity<Response<ProposedMinedBlockResponse>> proposedMinedBlock(@RequestBody SignedRequest<ProposedMinedBlockRequestBody> request) {
        var v = validator.validate(request);
        if(!v.valid()) {
            return buildResponse(new ErrorResponse<>(v, proxySignatureSuite));
        }

        Response<ProposedMinedBlockResponse> response = ledgerProxy.invokeOrdered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }
}