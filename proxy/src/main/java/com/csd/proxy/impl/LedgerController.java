package com.csd.proxy.impl;

import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.item.*;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.SignedRequest;
import com.csd.common.response.wrapper.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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
        this.proxySignatureSuite = new SignatureSuite(new SuiteConfiguration(
                new IniSpecification("proxy_signature_suite", CRYPTO_CONFIG_PATH),
                new StoredSecrets(new KeyStoresInfo("stores", CRYPTO_CONFIG_PATH))
        ), SignatureSuite.Mode.Both);
    }

    @PostMapping("/load")
    public ResponseEntity<Response<TransactionDetails>> loadMoney(@RequestBody SignedRequest<LoadMoneyRequestBody> request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getId()));
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<TransactionDetails> response = ledgerProxy.invokeOrdered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/balance")
    public ResponseEntity<Response<Double>> getBalance(@RequestBody SignedRequest<GetBalanceRequestBody> request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getId()));
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<Double> response = ledgerProxy.invokeUnordered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Response<TransactionDetails>> sendTransaction(@RequestBody SignedRequest<SendTransactionRequestBody> request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getId()));
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<TransactionDetails> response = ledgerProxy.invokeOrdered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/extract")
    public ResponseEntity<Response<ArrayList<Transaction>>> getExtract(@RequestBody SignedRequest<GetExtractRequestBody> request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getId()));
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<ArrayList<Transaction>> response = ledgerProxy.invokeUnordered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/total")
    public ResponseEntity<Response<Double>> getTotalValue(@RequestBody GetTotalValueRequestBody request) {
        for(SignedRequest<Request.Void> signedRequest : request.getListOfAccounts()){
            var v = validator.validate(signedRequest, ledgerProxy.getLastTrxDate(signedRequest.getId()));
            if(!v.valid()) {
                return buildResponse(new Response<>(v, proxySignatureSuite));
            }
        }

        Response<Double> response = ledgerProxy.invokeUnordered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/global")
    public ResponseEntity<Response<Double>> getGlobalValue(@RequestBody GetGlobalValueRequestBody request) {
        Response<Double> response = ledgerProxy.invokeUnordered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/ledger")
    public ResponseEntity<Response<ArrayList<Transaction>>> getLedger(@RequestBody GetLedgerRequestBody request) {
        Response<ArrayList<Transaction>> response = ledgerProxy.invokeUnordered(request);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }
}