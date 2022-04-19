package com.csd.common.cryptography.suites.digest;


import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.ISuiteSpecification;
import com.csd.common.cryptography.key.KeyInfo;

import java.util.Collections;
import java.util.Set;

public class FlexibleDigestSuite implements IDigestSuite {

	private final IDigestSuite suite;
	
	public FlexibleDigestSuite(ISuiteConfiguration config, SignatureSuite.Mode mode) throws Exception {
		String type = config.getString("type");
		switch (type) {
			case "Hmac":
				suite = new HMacSuite(config);
				break;
			case "Cmac":
				suite = new CMacSuite(config);
				break;
			case "Hash":
				suite = new HashSuite(config);
				break;
			case "Signature":
				suite = new SignatureSuite(config, mode);
				break;
			default:
				throw new Exception("Invalid disgest type " + type);
		}
	}

	@Override
	public byte[] digest(byte[] text) throws Exception {
		return suite.digest(text);
	}

	@Override
	public boolean verify(byte[] text, byte[] digest) throws Exception {
		return suite.verify(text, digest);
	}

	public static Set<KeyInfo> requiredKeys(ISuiteSpecification spec, SignatureSuite.Mode mode) throws Exception {
		String type = spec.getString("type");
		switch (type) {
			case "Hmac":
				return Collections.singleton(HMacSuite.requiredKey(spec));
			case "Cmac":
				return Collections.singleton(CMacSuite.requiredKey(spec));
			case "Hash":
				return Collections.emptySet();
			case "Signature":
				return SignatureSuite.requiredKeys(spec, mode);
			default:
				throw new Exception("Invalid disgest type " + type);
		}
	}

}