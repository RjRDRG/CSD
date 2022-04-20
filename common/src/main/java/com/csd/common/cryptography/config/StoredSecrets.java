package com.csd.common.cryptography.config;

import com.csd.common.cryptography.key.KeyStoresInfo;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class StoredSecrets implements ISuiteSecrets {

	private PasswordProtection password;
	private final KeyStore keyStore;
	private final KeyStore trustStore;
	
	public StoredSecrets(KeyStoresInfo storesConfig) throws Exception {
		this.password = new PasswordProtection(storesConfig.password);
		
		this.keyStore = KeyStore.getInstance(storesConfig.keystoreType);
		this.keyStore.load(new FileInputStream(storesConfig.keystorePath), storesConfig.password);

		this.trustStore = KeyStore.getInstance(storesConfig.truststoreType);
		this.trustStore.load(new FileInputStream(storesConfig.truststorePath), storesConfig.password);
	}
	
	@Override
	public Key getKey(String id) throws Exception {
		return keyStore.getKey(id, password.getPassword());
	}

	@Override
	public PrivateKey getPrivateKey(String id) throws Exception {
		return (PrivateKey) keyStore.getKey(id, password.getPassword());
	}
	
	@Override
	public Certificate getCertificate(String id) throws Exception {
		return trustStore.getCertificate(id);
	}
}
