package com.csd.common.item;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.hlib.HomoAdd;
import com.csd.common.cryptography.hlib.PaillierKey;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;

import java.nio.charset.StandardCharsets;

import static com.csd.common.util.Serialization.concat;

public class Wallet {
    static final String SECURITY_CONF = "security.conf";

    public final byte[] clientId;

    public final FlexibleDigestSuite clientIdDigestSuite;
    public final SignatureSuite signatureSuite;
    public final PaillierKey pk;

    public Wallet(String email, String seed) throws Exception {
        ISuiteConfiguration clientIdSuiteConfiguration =
                new SuiteConfiguration(
                        new IniSpecification("client_id_digest_suite", SECURITY_CONF),
                        new StoredSecrets(new KeyStoresInfo("stores", SECURITY_CONF))
                );
        clientIdDigestSuite = new FlexibleDigestSuite(clientIdSuiteConfiguration, SignatureSuite.Mode.Digest);

        this.signatureSuite = new SignatureSuite(
                new IniSpecification("client_signature_suite", SECURITY_CONF),
                new IniSpecification("client_signature_keygen_suite", SECURITY_CONF)
        );

        this.pk = HomoAdd.generateKey();

        byte[] provenance = concat(
                email.getBytes(StandardCharsets.UTF_8),
                seed.getBytes(StandardCharsets.UTF_8)
        );

        this.clientId = concat(
                clientIdDigestSuite.digest(provenance),
                clientIdDigestSuite.digest(signatureSuite.getPublicKey().getEncoded())
        );
    }
}
