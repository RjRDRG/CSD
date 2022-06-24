package com.csd.client;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;

public class WalletDetails {
    static final String SECURITY_CONF = "security.conf";

    public final byte[] clientId;
    public final SignatureSuite signatureSuite;

    public WalletDetails(String email, String seed) throws Exception {
        ISuiteConfiguration clientIdSuiteConfiguration =
                new SuiteConfiguration(
                        new IniSpecification("client_id_digest_suite", SECURITY_CONF),
                        new StoredSecrets(new KeyStoresInfo("stores", SECURITY_CONF))
                );
        FlexibleDigestSuite clientIdDigestSuite = new FlexibleDigestSuite(clientIdSuiteConfiguration, SignatureSuite.Mode.Digest);

        this.signatureSuite = new SignatureSuite(
                new IniSpecification("client_signature_suite", SECURITY_CONF),
                new IniSpecification("client_signature_keygen_suite", SECURITY_CONF)
        );

        byte[] provenance = ArrayUtils.addAll(
                email.getBytes(StandardCharsets.UTF_8),
                seed.getBytes(StandardCharsets.UTF_8)
        );

        this.clientId = ArrayUtils.addAll(
                clientIdDigestSuite.digest(provenance),
                clientIdDigestSuite.digest(signatureSuite.getPublicKey().getEncoded())
        );
    }
}
