package com.csd.common.cryptography.suites.digest;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.ISuiteSpecification;

import java.nio.charset.StandardCharsets;
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

	public HashSuite(ISuiteSpecification spec) throws Exception {
		String alg = spec.getString("alg");
		String provider = spec.getString("provider");
		if(provider != null)
			this.suite = MessageDigest.getInstance(alg, provider);
		else
			this.suite = MessageDigest.getInstance(alg);
	}
	
	@Override
	public byte[] digest(byte[] input) {
		return suite.digest(input);
	}

	public String digest(String input) {
		return new String(digest(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
	}

	@Override
	public boolean verify(byte[] text, byte[] digest) throws Exception {
		return Arrays.equals(digest(text), digest);
	}
}
