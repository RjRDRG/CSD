package com.csd.proxy.impl;

import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.key.ExperimentalKeyRegistry;
import com.csd.common.cryptography.key.IKeyRegistry;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.item.*;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.Response;
import com.csd.common.traits.Signature;
import com.csd.common.util.Status;
import com.csd.proxy.impl.pow.PowProxy;
import com.csd.proxy.ledger.ResourceEntity;
import com.csd.proxy.ledger.ResourceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.dataToJson;
import static com.csd.proxy.exceptions.ResponseEntityBuilder.buildResponse;

@RestController
@ConditionalOnProperty("proxy.pow")
class LedgerPowController {
    private final PowProxy ledgerProxy;
    private final RequestValidator validator;

    private final ResourceRepository ledger;
    private final SignatureSuite proxySignatureSuite;

    private final int quorum;

    LedgerPowController(PowProxy ledgerProxy, Environment environment) throws Exception {
        this.ledgerProxy = ledgerProxy;
        this.ledger = ledgerProxy.resourceRepository;
        this.quorum = environment.getProperty("proxy.quorum.size" , int.class);
        this.validator = new RequestValidator(quorum, environment.getProperty("proxy.number" , int.class));
        this.proxySignatureSuite = new SignatureSuite(new IniSpecification("proxy_signature_suite", CRYPTO_CONFIG_PATH));
        IKeyRegistry keyRegistry = new ExperimentalKeyRegistry();
        this.proxySignatureSuite.setPublicKey(keyRegistry.getProxyKey(environment.getProperty("proxy.id" , int.class)));
        this.proxySignatureSuite.setPrivateKey(keyRegistry.getProxyPrivateKey(environment.getProperty("proxy.id" , int.class)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Response<SendTransactionRequestBody>> sendTransaction(@RequestBody SendTransactionRequestBody request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        if (request.getAmount()<0 || request.getFee() < 0)
            return buildResponse(new Response<>(Status.BAD_REQUEST, "Transaction amount must be positive", proxySignatureSuite));

        double balance = 0;
        balance += ledger.findByOwner(bytesToString(request.getClientId().get(0))).stream()
                .filter(t -> t.getType().equals(Resource.Type.VALUE.name()))
                .map(ResourceEntity::getAsset)
                .map(Double::valueOf)
                .reduce(0.0, Double::sum);

        if (balance<request.getAmount())
            return buildResponse(new Response<>(Status.FORBIDDEN, "Insufficient Credit", proxySignatureSuite));


        request.addProxySignature(new Signature(proxySignatureSuite, request.serializedSignedRequest()));

        Response<SendTransactionRequestBody> response;
        if(request.getProxySignatures().size() == quorum) {
            response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.TRANSFER);
        } else {
            response = new Response<>(request);
        }
        response.proxySignature(proxySignatureSuite);
        return buildResponse(response);
    }

    @PostMapping("/transfer/once")
    public ResponseEntity<Response<SendTransactionRequestBody>> sendTransactionOnce(@RequestBody SendTransactionRequestBody request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        if (request.getAmount()<0 || request.getFee() < 0)
            return buildResponse(new Response<>(Status.BAD_REQUEST, "Transaction amount must be positive", proxySignatureSuite));

        double balance = 0;
        balance += ledger.findByOwner(bytesToString(request.getClientId().get(0))).stream()
                .filter(t -> t.getType().equals(Resource.Type.VALUE.name()))
                .map(ResourceEntity::getAsset)
                .map(Double::valueOf)
                .reduce(0.0, Double::sum);

        if (balance<request.getAmount())
            return buildResponse(new Response<>(Status.FORBIDDEN, "Insufficient Credit", proxySignatureSuite));


        request.addProxySignature(new Signature(proxySignatureSuite, request.serializedSignedRequest()));

        Response<SendTransactionRequestBody> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.TRANSFER);
        response.proxySignature(proxySignatureSuite);
        return buildResponse(response);
    }

    @PostMapping("/load")
    public ResponseEntity<Response<LoadMoneyRequestBody>> loadMoney(@RequestBody LoadMoneyRequestBody request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<LoadMoneyRequestBody> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.LOAD);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/decrypt")
    public ResponseEntity<Response<Double>> decryptValueAsset(@RequestBody DecryptValueAssetRequestBody request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<Double> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.DECRYPT);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/balance")
    public ResponseEntity<Response<Double>> getBalance(@RequestBody GetBalanceRequestBody request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<Double> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.BALANCE);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/balance/encrypted")
    public ResponseEntity<Response<byte[]>> getEncryptedBalance(@RequestBody GetEncryptedBalanceRequestBody request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<byte[]> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.HIDDEN);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/extract")
    public ResponseEntity<Response<ArrayList<Resource>>> getExtract(@RequestBody GetExtractRequestBody request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<ArrayList<Resource>> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.EXTRACT);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/total")
    public ResponseEntity<Response<Double>> getTotalValue(@RequestBody GetTotalValueRequestBody request) {
        var v = validator.validate(request, ledgerProxy.getLastTrxDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<Double> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.TOTAL_VAL);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/global")
    public ResponseEntity<Response<Double>> getGlobalValue(@RequestBody GetGlobalValueRequestBody request) {
        Response<Double> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.GLOBAL_VAL);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/ledger")
    public ResponseEntity<Response<ArrayList<Resource>>> getLedger(@RequestBody GetLedgerRequestBody request) {
        Response<ArrayList<Resource>> response = ledgerProxy.invokeUnordered(request, ConsensusRequest.Type.LEDGER);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @GetMapping("/block")
    public ResponseEntity<Long> getBlock() {
        return new ResponseEntity<>(Optional.ofNullable(ledger.findTopByOrderByIdDesc()).map(ResourceEntity::getBlock).orElse(0L), HttpStatus.OK);
    }
}