
package com.csd.replica.impl;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.item.RequestInfo;
import com.csd.common.item.Transaction;
import com.csd.common.request.GetBalanceRequestBody;
import com.csd.common.request.LoadMoneyRequestBody;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Service
public class LedgerService {

    public static final String CONFIG_PATH = "security.conf";

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final IDigestSuite clientIdDigestSuite;
    private final SignatureSuite clientSignatureSuite;

    public LedgerService() throws Exception {
        ISuiteConfiguration clientIdSuiteConfiguration =
                new SuiteConfiguration(
                        new IniSpecification("client_id_digest_suite", CONFIG_PATH),
                        new StoredSecrets(new KeyStoresInfo("stores",CONFIG_PATH))
                );
        this.clientIdDigestSuite = new FlexibleDigestSuite(clientIdSuiteConfiguration, SignatureSuite.Mode.Verify);

        this.clientSignatureSuite = new SignatureSuite(new IniSpecification("client_signature_suite", CONFIG_PATH));
    }

    public <T extends Serializable, R extends Serializable> Result<R> validateProtectedRequest(ProtectedRequest<T> request) {
        try {
            boolean valid = request.verifyClientId(clientIdDigestSuite) && request.verifySignature(clientSignatureSuite);
            if (!valid) return Result.error(Result.Status.FORBIDDEN, "Invalid Signature");
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
        return Result.ok();
    }

    public <T extends Serializable, R extends Serializable> Result<R> validateAuthenticatedRequest(AuthenticatedRequest<T> request) {
        try {
            boolean valid = request.verifyClientId(clientIdDigestSuite) && request.verifySignature(clientSignatureSuite);
            if (!valid) return Result.error(Result.Status.FORBIDDEN, "Invalid Signature");
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
        return Result.ok();
    }

    public Result<RequestInfo> loadMoney(ProtectedRequest<LoadMoneyRequestBody> request, OffsetDateTime timestamp) {
        Result<RequestInfo> validationResult = validateProtectedRequest(request);
        if(!validationResult.isOK())
            return validationResult;

        return Result.ok(new RequestInfo(timestamp));
    }

    public Result<Double> getBalance(AuthenticatedRequest<GetBalanceRequestBody> request) {
        Result<Double> validationResult = validateAuthenticatedRequest(request);
        if(!validationResult.isOK())
            return validationResult;

        return Result.ok(14.0);
    }
}