package com.csd.common.cryptography.config;

import java.math.BigInteger;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class SuiteConfiguration implements ISuiteConfiguration {

	private final ISuiteSpecification spec;
	private final ISuiteSecrets secrets;

	public SuiteConfiguration(ISuiteSpecification spec, ISuiteSecrets secrets) {
		this.spec = spec;
		this.secrets = secrets;
	}

	@Override
	public ISuiteConfiguration getSubConfiguration(String id) throws Exception {
		return new SuiteConfiguration(getSubSpec(id), secrets);
	}

	@Override
	public ISuiteSpecification getSubSpec(String id) throws Exception {
		return spec.getSubSpec(id);
	}

	@Override
	public Key getKey(String id) throws Exception {
		return secrets.getKey(id);
	}

	@Override
	public PrivateKey getPrivateKey(String id) throws Exception {
		return secrets.getPrivateKey(id);
	}
	
	@Override
	public Certificate getCertificate(String id) throws Exception {
		return secrets.getCertificate(id);
	}

	@Override
	public boolean getBoolean(String id) {
		return spec.getBoolean(id);
	}

	@Override
	public int getInt(String id) throws Exception {
		return spec.getInt(id);
	}

	@Override
	public float getFloat(String id) throws Exception {
		return spec.getFloat(id);
	}

	@Override
	public BigInteger getBigInteger(String id) throws Exception {
		return spec.getBigInteger(id);
	}

	@Override
	public String getString(String id) throws Exception {
		return spec.getString(id);
	}

	@Override
	public byte[] getBytes(String id) throws Exception {
		return spec.getBytes(id);
	}

}
