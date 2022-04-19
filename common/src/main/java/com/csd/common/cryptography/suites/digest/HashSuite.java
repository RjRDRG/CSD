package com.csd.common.cryptography.suites.digest;

import com.csd.common.cryptography.config.ISuiteConfiguration;

import java.security.MessageDigest;
import java.util.Arrays;

public class HashSuite implements IDigestSuite{

	private final MessageDigest suite;
	
	public HashSuite(ISuiteConfiguration config) throws Exception {
		String alg = config.getString("alg");
		String provider = config.getString("provider");
		if(provider != null)
			this.suite = MessageDigest.getInstance(alg, provider);
		else
			this.suite = MessageDigest.getInstance(alg);
	}
	
	@Override
	public byte[] digest(byte[] input) {
		return suite.digest(input);
	}

	@Override
	public boolean verify(byte[] text, byte[] digest) throws Exception {
		return Arrays.equals(digest(text), digest);
	}
}
