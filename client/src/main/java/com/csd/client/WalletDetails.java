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
import java.util.Arrays;

public class WalletDetails {
    static final String SECURITY_CONF = "security.conf";

    public final byte[] clientId;
    public final EncodedPublicKey clientPublicKey;
    public final SignatureSuite signatureSuite;
    public Long nonce;

    public WalletDetails(String email, String account) throws Exception {
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
        this.clientPublicKey = signatureSuite.getPublicKey();

        byte[] provenance = clientIdDigestSuite.digest(ArrayUtils.addAll(
                email.getBytes(StandardCharsets.UTF_8),
                account.getBytes(StandardCharsets.UTF_8)
        ));

        this.clientId = ArrayUtils.addAll(
                provenance,
                clientPublicKey.getEncoded()
        );

        this.nonce = null;
    }

    long getNonce() {
        return ++nonce;
    }
}
