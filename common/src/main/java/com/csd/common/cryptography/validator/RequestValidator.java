package com.csd.common.cryptography.validator;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.ExperimentalKeyRegistry;
import com.csd.common.cryptography.key.IPubKeyRegistry;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.request.Request;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.csd.common.Constants.CRYPTO_CONFIG_PATH;

public class RequestValidator {
    private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);

    private final IDigestSuite clientIdDigestSuite;
    private final SignatureSuite clientSignatureSuite;

    private final List<SignatureSuite> proxySignatureSuites;

    private final int q;

    public RequestValidator(int q) throws Exception {
        ISuiteConfiguration clientIdSuiteConfiguration =
                new SuiteConfiguration(
                        new IniSpecification("client_id_digest_suite", CRYPTO_CONFIG_PATH),
                        new StoredSecrets(new KeyStoresInfo("stores", CRYPTO_CONFIG_PATH))
                );
        this.clientIdDigestSuite = new FlexibleDigestSuite(clientIdSuiteConfiguration, SignatureSuite.Mode.Verify);

        this.clientSignatureSuite = new SignatureSuite(new IniSpecification("client_signature_suite", CRYPTO_CONFIG_PATH));

        this.proxySignatureSuites = new ArrayList<>(4);

        IPubKeyRegistry pubKeyRegistry = new ExperimentalKeyRegistry();

        for (int i=0; i<4; i++) {
            SignatureSuite s = new SignatureSuite(new IniSpecification("proxy_signature_suite", CRYPTO_CONFIG_PATH));
            s.setPublicKey(pubKeyRegistry.getProxyKey(i));
            proxySignatureSuites.add(s);
        }

        this.q = q;
    }

    public <R extends Request> Result<R> validate(R request, OffsetDateTime nonce, boolean validateProxySig) {
        try {
            if (!request.verifyClientSignature(clientIdDigestSuite, clientSignatureSuite))
                return Result.error(Status.FORBIDDEN, "Invalid Signature: " + request);

            if (!request.verifyNonce(nonce))
                return Result.error(Status.FORBIDDEN, "Invalid Nonce: " + request);

            if(validateProxySig && !request.verifyProxySignatures(proxySignatureSuites, q))
                return Result.error(Status.FORBIDDEN, "Invalid Proxy Signature: " + request);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(Status.INTERNAL_ERROR, e.getMessage() + ": " + request);
        }
        return Result.ok(request);
    }

}
