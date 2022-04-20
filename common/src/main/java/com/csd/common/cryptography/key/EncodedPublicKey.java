package com.csd.common.cryptography.key;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.bytesToString;

public class EncodedPublicKey implements Serializable {
	private static final long serialVersionUID = -1440213254532977043L;
	
	byte[] encoded;
	String alg;

	public EncodedPublicKey(PublicKey key) {
		this.alg = key.getAlgorithm();
		this.encoded = key.getEncoded();
	}
	
	public PublicKey toPublicKey() throws Exception {
		return KeyFactory.getInstance(alg).generatePublic(new X509EncodedKeySpec(encoded));
	}
	
	EncodedPublicKey() {
	}

	public String getAlg() {
		return alg;
	}

	public void setAlg(String alg) {
		this.alg = alg;
	}
	
	public byte[] getEncoded() {
		return encoded;
	}

	public void setEncoded(byte[] encoded) {
		this.encoded = encoded;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EncodedPublicKey that = (EncodedPublicKey) o;
		return Arrays.equals(encoded, that.encoded) && alg.equals(that.alg);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(alg);
		result = 31 * result + Arrays.hashCode(encoded);
		return result;
	}

	@Override
	public String toString() {
		return "EncodedPublicKey{" +
				"enconded=" + bytesToString(encoded) +
				", alg='" + alg + '\'' +
				'}';
	}
}
