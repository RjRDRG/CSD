package com.csd.common.cryptography.suites.digest;

import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.ISuiteSpecification;
import com.csd.common.cryptography.key.KeyInfo;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.security.Key;
import java.util.Arrays;

public class CMacSuite implements IDigestSuite {

	private final CMac cmac;

	public CMacSuite(ISuiteConfiguration config) throws Exception {
		String alg = config.getString("alg");
		BlockCipher cipher;
		switch (alg) {
			case "AES": {
				cipher = new AESEngine();
				break;
			}
			case "DES": {
				cipher = new DESEngine();
				break;
			}
			case "Blowfish": {
				cipher = new BlowfishEngine();
				break;
			}
			default: {
				cipher = new AESEngine();
				break;
			}
		}
		this.cmac = new CMac(cipher);

		String keyAlias = config.getString("keyAlias");
		Key key = config.getKey(keyAlias);
		cmac.init(new KeyParameter(key.getEncoded()));
	}

	@Override
	public byte[] digest(byte[] input) {
		byte[] output = new byte[cmac.getMacSize()];
		cmac.update(input, 0, input.length);
		cmac.doFinal(output, 0);
		return output;
	}

	@Override
	public boolean verify(byte[] text, byte[] digest) throws Exception {
		return Arrays.equals(digest(text), digest);
	}

	public static KeyInfo requiredKey(ISuiteSpecification spec) throws Exception {
		String alg = spec.getString("alg");
		String keyAlias = spec.getString("keyAlias");
		Integer keyLenght = spec.getInt("keyLenght");
		return new KeyInfo(keyAlias, alg, keyLenght, KeyInfo.Type.Secret);
	}

}
