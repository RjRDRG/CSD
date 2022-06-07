package com.csd.common.cryptography.validator;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.IRequest;
import com.csd.common.request.wrapper.SignedRequest;
import com.csd.common.request.wrapper.UniqueRequest;
import com.csd.common.traits.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;

public class RequestValidator {
    private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);

    private final IDigestSuite clientIdDigestSuite;
    private final SignatureSuite clientSignatureSuite;

    public RequestValidator() throws Exception {
        ISuiteConfiguration clientIdSuiteConfiguration =
                new SuiteConfiguration(
                        new IniSpecification("client_id_digest_suite", CRYPTO_CONFIG_PATH),
                        new StoredSecrets(new KeyStoresInfo("stores", CRYPTO_CONFIG_PATH))
                );
        this.clientIdDigestSuite = new FlexibleDigestSuite(clientIdSuiteConfiguration, SignatureSuite.Mode.Verify);

        this.clientSignatureSuite = new SignatureSuite(new IniSpecification("client_signature_suite", CRYPTO_CONFIG_PATH));
    }

    public <R extends IRequest> Result<UniqueRequest<R>> validate(UniqueRequest<R> request, long nonce) {
        try {
            if (!request.verifyId(clientIdDigestSuite))
                return Result.error(Result.Status.FORBIDDEN, "Invalid Id");

            if (!request.verifySignature(clientSignatureSuite))
                return Result.error(Result.Status.FORBIDDEN, "Invalid Signature");

            if (!request.verifyNonce(nonce))
                return Result.error(Result.Status.FORBIDDEN, "Invalid Nonce: " + nonce + " " + request.getNonce());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
        return Result.ok(request);
    }

    public <R extends IRequest> Result<UniqueRequest<R>> validate(UniqueRequest<R> request) {
        try {
            if (!request.verifyId(clientIdDigestSuite))
                return Result.error(Result.Status.FORBIDDEN, "Invalid Id");

            if (!request.verifySignature(clientSignatureSuite))
                return Result.error(Result.Status.FORBIDDEN, "Invalid Signature");
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
        return Result.ok(request);
    }

    public <R extends IRequest> Result<SignedRequest<R>> validate(SignedRequest<R> request) {
        try {
            if (!request.verifyId(clientIdDigestSuite))
                return Result.error(Result.Status.FORBIDDEN, "Invalid Signature");

            if (!request.verifySignature(clientSignatureSuite))
                return Result.error(Result.Status.FORBIDDEN, "Invalid Signature");
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
        return Result.ok(request);
    }

}
