package com.csd.common.cryptography.suites.digest;
import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.ISuiteSpecification;
import com.csd.common.cryptography.generators.AsymmetricKeyPairGenerator;
import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.key.KeyInfo;

import java.security.*;
import java.util.HashSet;
import java.util.Set;

public class SignatureSuite implements IDigestSuite {
	public enum Mode {
		Digest, Verify, Both
	}

	private Signature suite;

	private final String alg;
	private final String provider;
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public SignatureSuite(ISuiteConfiguration config, Mode mode) throws Exception {
		alg = config.getString("alg");
		provider = config.getString("provider");
		suite = getInstance();
		
		String keyAlias = config.getString("keyAlias");
		switch (mode) {
			case Digest:
				publicKey = null;
				privateKey = config.getPrivateKey(keyAlias);
				break;
			case Verify:
				publicKey = config.getCertificate(keyAlias).getPublicKey();
				privateKey = null;
				break;
			case Both:
				publicKey = config.getCertificate(keyAlias).getPublicKey();
				privateKey = config.getPrivateKey(keyAlias);
		}
	}
	
	public SignatureSuite(ISuiteSpecification spec) throws Exception {
		alg = spec.getString("alg");
		provider = spec.getString("provider");
		suite = getInstance();
		privateKey = null;
		publicKey = null;
	}

	public SignatureSuite(ISuiteSpecification spec, ISuiteSpecification keyGenspec) throws Exception {
		alg = spec.getString("alg");
		provider = spec.getString("provider");
		suite = getInstance();
		AsymmetricKeyPairGenerator keyGen = new AsymmetricKeyPairGenerator(keyGenspec);
		KeyPair keyPair = keyGen.generateKeyPair();
		privateKey = keyPair.getPrivate();
		publicKey = keyPair.getPublic();
	}
	
	public SignatureSuite(ISuiteSpecification spec, PublicKey pubKey) throws Exception {
		alg = spec.getString("alg");
		provider = spec.getString("provider");
		suite = getInstance();
		publicKey = pubKey;
		privateKey = null;
	}

	private Signature getInstance() throws Exception {
		if(provider != null)
			return Signature.getInstance(alg, provider);
		else
			return Signature.getInstance(alg);
	}

	@Override
	public byte[] digest(byte[] plainText) throws Exception {
		Signature suite = getInstance();
		suite.initSign(privateKey);
		suite.update(plainText);
		return suite.sign();
	}

	@Override
	public boolean verify(byte[] data, byte[] signature) throws Exception {
		Signature suite = getInstance();
		suite.initVerify(publicKey);
		suite.update(data);
		return suite.verify(signature);
	}

	public boolean verify(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
		Signature suite = getInstance();
		suite.initVerify(publicKey);
		suite.update(data);
		return suite.verify(signature);
	}

	public void setPublicKey(EncodedPublicKey key) throws Exception {
		this.publicKey = key.toPublicKey();
	}

	public void setPrivateKey(PrivateKey key) {
		this.privateKey = key;
	}

	public EncodedPublicKey getPublicKey() {
		return new EncodedPublicKey(publicKey);
	}

	public static Set<KeyInfo> requiredKeys(ISuiteSpecification spec, Mode mode) throws Exception {
		String alg = spec.getString("alg").split("/")[0];
		String keyAlias = spec.getString("keyAlias");
		Set<KeyInfo> keys = new HashSet<>(2);
		switch (mode) {
			case Digest:
				keys.add(new KeyInfo(keyAlias, alg, null, KeyInfo.Type.Private));
				break;
			case Verify:
				keys.add(new KeyInfo(keyAlias, alg, null, KeyInfo.Type.Public));
				break;
			case Both:
				keys.add(new KeyInfo(keyAlias, alg, null, KeyInfo.Type.Private));
				keys.add(new KeyInfo(keyAlias, alg, null, KeyInfo.Type.Public));
				break;
		}
		return keys;
	}


}
