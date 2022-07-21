package com.csd.proxy.impl;

import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.key.ExperimentalKeyRegistry;
import com.csd.common.cryptography.key.IKeyRegistry;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.item.Resource;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.Response;
import com.csd.common.traits.Signature;
import com.csd.common.util.Status;
import com.csd.proxy.impl.blockmess.BlockmessProxyOrderer;
import com.csd.proxy.ledger.ResourceEntity;
import com.csd.proxy.ledger.ResourceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;
import static com.csd.common.util.Serialization.bytesToString;
import static com.csd.common.util.Serialization.dataToJson;
import static com.csd.proxy.exceptions.ResponseEntityBuilder.buildResponse;

@RestController
@ConditionalOnProperty("proxy.blockmess")
class LedgerBlockmessController {
    private final BlockmessProxyOrderer orderer;
    private final RequestValidator validator;

    private final ResourceRepository ledger;
    private final SignatureSuite proxySignatureSuite;

    private final int quorum;

    LedgerBlockmessController(BlockmessProxyOrderer orderer, Environment environment) throws Exception {
        System.out.println("Constructor");
        this.orderer = orderer;
        this.ledger = orderer.resourceRepository;
        this.quorum = environment.getProperty("proxy.quorum.size" , int.class);
        this.validator = new RequestValidator(quorum, environment.getProperty("proxy.number" , int.class));
        this.proxySignatureSuite = new SignatureSuite(new IniSpecification("proxy_signature_suite", CRYPTO_CONFIG_PATH));
        IKeyRegistry keyRegistry = new ExperimentalKeyRegistry();
        this.proxySignatureSuite.setPublicKey(keyRegistry.getProxyKey(environment.getProperty("proxy.id" , int.class)));
        this.proxySignatureSuite.setPrivateKey(keyRegistry.getProxyPrivateKey(environment.getProperty("proxy.id" , int.class)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Response<SendTransactionRequestBody>> sendTransaction(@RequestBody SendTransactionRequestBody request) {
        System.out.println("Transfer");
        var v = validator.validate(request, orderer.getLastResourceDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            System.out.println("Invalid Request");
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

        request.addProxySignature(new Signature(proxySignatureSuite,request.serializedSignedRequest()));

        Response<SendTransactionRequestBody> response = orderer.invoke(request, ConsensusRequest.Type.TRANSFER);
        response.proxySignature(proxySignatureSuite);
        return buildResponse(response);
    }

    @PostMapping("/transfer/once")
    public ResponseEntity<Response<SendTransactionRequestBody>> sendTransactionOnce(@RequestBody SendTransactionRequestBody request) {
        return sendTransaction(request);
    }

    @PostMapping("/load")
    public ResponseEntity<Response<LoadMoneyRequestBody>> loadMoney(@RequestBody LoadMoneyRequestBody request) {
        System.out.println("Load");
        var v = validator.validate(request, orderer.getLastResourceDate(request.getClientId().get(0)), false);
        if(!v.valid()) {
            System.out.println("Invalid " + v.message());
            return buildResponse(new Response<>(v, proxySignatureSuite));
        }

        Response<LoadMoneyRequestBody> response = orderer.invoke(request, ConsensusRequest.Type.LOAD);
        response.proxySignature(proxySignatureSuite);

        return buildResponse(response);
    }

    @PostMapping("/balance")
    public ResponseEntity<Response<Double>> getBalance(@RequestBody GetBalanceRequestBody request) {
        return buildResponse(new Response<>(Status.NOT_IMPLEMENTED,""));
    }

    @PostMapping("/decrypt")
    public ResponseEntity<Response<Double>> decryptValueAsset(@RequestBody DecryptValueAssetRequestBody request) {
        return buildResponse(new Response<>(Status.NOT_IMPLEMENTED,""));
    }

    @PostMapping("/balance/encrypted")
    public ResponseEntity<Response<byte[]>> getEncryptedBalance(@RequestBody GetEncryptedBalanceRequestBody request) {
        return buildResponse(new Response<>(Status.NOT_IMPLEMENTED,""));
    }

    @PostMapping("/extract")
    public ResponseEntity<Response<ArrayList<Resource>>> getExtract(@RequestBody GetExtractRequestBody request) {
        return buildResponse(new Response<>(Status.NOT_IMPLEMENTED,""));
    }

    @PostMapping("/total")
    public ResponseEntity<Response<Double>> getTotalValue(@RequestBody GetTotalValueRequestBody request) {
        return buildResponse(new Response<>(Status.NOT_IMPLEMENTED,""));
    }

    @PostMapping("/global")
    public ResponseEntity<Response<Double>> getGlobalValue(@RequestBody GetGlobalValueRequestBody request) {
        return buildResponse(new Response<>(Status.NOT_IMPLEMENTED,""));
    }

    @PostMapping("/ledger")
    public ResponseEntity<Response<ArrayList<Resource>>> getLedger(@RequestBody GetLedgerRequestBody request) {
        return buildResponse(new Response<>(Status.NOT_IMPLEMENTED,""));
    }
}