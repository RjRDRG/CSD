package com.csd.common.traits;

import com.csd.common.cryptography.suites.digest.IDigestSuite;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.csd.common.util.Serialization.dataToJson;

public class UniqueSeal<T extends Serializable> implements Serializable {

	T data;
	double nonce;
	byte[] signature;

	public UniqueSeal(T data, double nonce, IDigestSuite suite) throws Exception {
		this.data = data;
		this.nonce = nonce;
		this.signature = suite.digest(content());
	}

	private byte[] content() {
		return (dataToJson(data) + nonce).getBytes(StandardCharsets.UTF_8);
	}

	public boolean verify(IDigestSuite suite) throws Exception {
		return suite.verify(content(), signature);
	}

	public UniqueSeal() {
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public double getNonce() {
		return nonce;
	}

	public void setNonce(double nonce) {
		this.nonce = nonce;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	@Override
	public String toString() {
		return "UniqueSeal{" +
				"data=" + data +
				", nonce=" + nonce +
				", signature=" + Arrays.toString(signature) +
				'}';
	}
}
