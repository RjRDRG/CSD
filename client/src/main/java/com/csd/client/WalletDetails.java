package com.csd.client;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;

import static com.csd.common.util.Serialization.bytesToString;

public class WalletDetails {
    static final String SECURITY_CONF = "security.conf";

    public final byte[] clientId;
    public final EncodedPublicKey clientPublicKey;
    public final SignatureSuite signatureSuite;
    public long requestCounter;

    public WalletDetails() throws Exception {
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

        this.clientId = clientIdDigestSuite.digest(clientPublicKey.getEncoded());

        this.requestCounter = 0;
    }

    String getUrlSafeClientId() {
        return bytesToString(clientId);
    }

    long getRequestCounter() {
        return ++requestCounter;
    }
}
