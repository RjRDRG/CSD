package com.csd.common.cryptography.generators.key;

import com.csd.common.cryptography.config.ISuiteSpecification;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SymmetricKeyGenerator {
	
	KeyGenerator keyGen;
	
	public SymmetricKeyGenerator(ISuiteSpecification spec) throws Exception {
		String alg = spec.getString("alg");
		String provider = spec.getString("provider");	
		if (provider != null)
			keyGen = KeyGenerator.getInstance(alg, provider);
		else
			keyGen = KeyGenerator.getInstance(alg);
		
		int keyLength = spec.getInt("keyLength");
		keyGen.init(keyLength);
	}
	
	public SecretKey genKey() {
		return keyGen.generateKey();
	}

}
