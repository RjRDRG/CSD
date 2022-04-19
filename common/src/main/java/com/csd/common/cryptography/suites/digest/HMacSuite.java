package com.csd.common.cryptography.suites.digest;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.ISuiteSpecification;
import com.csd.common.cryptography.key.KeyInfo;

import javax.crypto.Mac;
import java.security.Key;
import java.util.Arrays;

public class HMacSuite implements IDigestSuite {

	private final Mac hmac;

	public HMacSuite(ISuiteConfiguration config) throws Exception {
		String alg = config.getString("alg");
		String provider = config.getString("provider");
		if(provider != null)
			this.hmac = Mac.getInstance(alg,provider);
		else
			this.hmac = Mac.getInstance(alg);
		
		String keyAlias = config.getString("keyAlias");
		Key key = config.getKey(keyAlias);
		hmac.init(key);
	}

	@Override
	public byte[] digest(byte[] input) {
		return hmac.doFinal(input);
	}

	@Override
	public boolean verify(byte[] text, byte[] digest) throws Exception {
		return Arrays.equals(digest(text), digest);
	}

	public static KeyInfo requiredKey(ISuiteSpecification spec) throws Exception {
		String alg = spec.getString("alg").split("/")[0];
		String keyAlias = spec.getString("keyAlias");
		Integer keyLenght = spec.getInt("keyLenght");
		return new KeyInfo(keyAlias, alg, keyLenght, KeyInfo.Type.Secret);
	}

}
