package com.csd.common.cryptography.suites.digest;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.ISuiteSpecification;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Arrays;

public class HashSuite implements IDigestSuite{

	private final MessageDigest suite;

	private final String alg;
	private final String provider;
	
	public HashSuite(ISuiteConfiguration config) throws Exception {
		alg = config.getString("alg");
		provider = config.getString("provider");
		suite = getInstance();
	}

	public HashSuite(ISuiteSpecification spec) throws Exception {
		alg = spec.getString("alg");
		provider = spec.getString("provider");
		suite = getInstance();
	}

	private MessageDigest getInstance() {
		try {
			if (provider != null)
				return MessageDigest.getInstance(alg, provider);
			else
				return MessageDigest.getInstance(alg);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	@Override
	public byte[] digest(byte[] input) {
		MessageDigest suite = getInstance();
		return suite.digest(input);
	}

	public String digest(String input) {
		MessageDigest suite = getInstance();
		return new String(digest(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
	}

	@Override
	public boolean verify(byte[] text, byte[] digest) throws Exception {
		return Arrays.equals(digest(text), digest);
	}
}
