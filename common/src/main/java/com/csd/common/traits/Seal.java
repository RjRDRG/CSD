package com.csd.common.traits;

import com.csd.common.cryptography.suites.digest.IDigestSuite;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static com.csd.common.util.Serialization.*;

public class Seal<T extends Serializable> implements Serializable {

	T data;
	byte[] signature;

	public Seal(T data, IDigestSuite suite) throws Exception {
		this.data = data;
		this.signature = suite.digest(dataToJson(data).getBytes(StandardCharsets.UTF_8));
	}

	public Seal(T data, byte[] signature) {
		this.data = data;
		this.signature = signature;
	}

	public boolean verify(IDigestSuite suite) throws Exception {
		return suite.verify(dataToJson(data).getBytes(StandardCharsets.UTF_8), signature);
	}

	public Seal() {
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	@Override
	public String toString() {
		return "Seal{" +
				"data=" + data.toString() +
				", signature=" + bytesToString(signature) +
				'}';
	}
}
