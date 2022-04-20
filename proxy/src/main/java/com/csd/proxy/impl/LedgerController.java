package com.csd.proxy.impl;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.item.*;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.proxy.exceptions.ForbiddenException;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

@RestController
class LedgerController {

    public static final String CONFIG_PATH = "security.conf";

    private final IDigestSuite clientIdDigestSuite;
    private final SignatureSuite clientSignatureSuite;

    //TODO validate nonce

    LedgerController() throws Exception {
        ISuiteConfiguration suiteConfiguration =
                new SuiteConfiguration(
                        new IniSpecification("client_id_digest_suite", CONFIG_PATH),
                        new StoredSecrets(new KeyStoresInfo("stores",CONFIG_PATH))
                );
        this.clientIdDigestSuite = new FlexibleDigestSuite(suiteConfiguration, SignatureSuite.Mode.Verify);
        this.clientSignatureSuite = new SignatureSuite(new IniSpecification("client_signature_suite", CONFIG_PATH));
    }

    private <T extends Serializable> void validRequest(ProtectedRequest<T> request) {
        boolean valid;
        try {
            valid = request.verifyClientId(clientIdDigestSuite) && request.verifySignature(clientSignatureSuite);
        } catch (Exception e) {
            throw new ForbiddenException(e.getMessage());
        }

        if(!valid) throw new ForbiddenException("Invalid Signature");
    }

    private <T extends Serializable> void validRequest(AuthenticatedRequest<T> request) {
        boolean valid;
        try {
            valid = request.verifyClientId(clientIdDigestSuite) && request.verifySignature(clientSignatureSuite);
        } catch (Exception e) {
            throw new ForbiddenException(e.getMessage());
        }

        if(!valid) throw new ForbiddenException("Invalid Signature");
    }

    @PostMapping("/load")
    public RequestInfo loadMoney(@RequestBody ProtectedRequest<LoadMoneyRequestBody> request) {

        validRequest(request);

        String requestId = UUID.randomUUID().toString();

        return new RequestInfo(requestId, Collections.emptyMap(), OffsetDateTime.now());
    }

    @PostMapping("/balance")
    public Double getBalance(@RequestBody AuthenticatedRequest<GetBalanceRequestBody> request) {

        validRequest(request);

        return 10.0;
    }
}