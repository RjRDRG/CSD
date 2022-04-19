package com.csd.common.cryptography.config;

import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public interface ISuiteSecrets {
	Key getKey(String id) throws Exception;
	PrivateKey getPrivateKey(String id) throws Exception;
	Certificate getCertificate(String id) throws Exception;
}
