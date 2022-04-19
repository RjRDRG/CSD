package com.csd.common.cryptography.config;

import com.csd.common.cryptography.generators.key.AsymmetricKeyPairGenerator;
import com.csd.common.cryptography.generators.key.SymmetricKeyGenerator;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class GeneratedSecrets implements ISuiteSecrets {
	private String filePath;
	private PasswordProtection password;
	private KeyStore keyStore;
	private SymmetricKeyGenerator keyGen;
	private AsymmetricKeyPairGenerator keyPairGen;

	public GeneratedSecrets(String filePath, char[] password) throws Exception {
		this.filePath = filePath;
		this.password = new PasswordProtection(password);
		File file = new File(filePath);
		this.keyStore = KeyStore.getInstance("PKCS12");
		if (file.exists()) {
			this.keyStore.load(new FileInputStream(file), password);
		} else {
			this.keyStore.load(null, null);
			this.keyStore.store(new FileOutputStream(filePath), password);
		}
	}

	public void setKeyGen(SymmetricKeyGenerator keyGen, AsymmetricKeyPairGenerator keyPairGen) {
		this.keyGen = keyGen;
		this.keyPairGen = keyPairGen;
	}

	@Override
	public Key getKey(String id) throws Exception {
		if (keyStore.containsAlias(id))
			return keyStore.getKey(id, password.getPassword());
		else {
			return createKey(id);
		}
	}

	private Key createKey(String id) throws Exception {
		SecretKey key = keyGen.genKey();
		KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(key);
		keyStore.setEntry(id, keyStoreEntry, password);
		keyStore.store(new FileOutputStream(filePath), password.getPassword());
		return key;
	}

	@Override
	public PrivateKey getPrivateKey(String id) throws Exception {
		if (keyStore.containsAlias(id + "private"))
			return (PrivateKey) keyStore.getKey(id + "private", password.getPassword());
		else {
			createKeyPair(id);
			return (PrivateKey) keyStore.getKey(id + "private", password.getPassword());
		}
	}

	@Override
	public Certificate getCertificate(String id) throws Exception {
		if (keyStore.containsAlias(id + "public"))
			return keyStore.getCertificate(id + "public");
		else {
			createKeyPair(id);
			return keyStore.getCertificate(id + "public");
		}
	}

	private void createKeyPair(String id) throws Exception {
		KeyPair keyPair = keyPairGen.generateKeyPair();
		Certificate certificate = keyPairGen.generateCertificate(keyPair);
		KeyStore.TrustedCertificateEntry trustedCertificateEntry = new KeyStore.TrustedCertificateEntry(certificate);
		KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), new Certificate[] { certificate });
		keyStore.setEntry(id + "private", privateKeyEntry, password);
		keyStore.setEntry(id + "public", trustedCertificateEntry, null);
		keyStore.store(new FileOutputStream(filePath), password.getPassword());
	}
}
