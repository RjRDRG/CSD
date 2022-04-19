package com.csd.common.cryptography.suites.digest;

public interface IDigestSuite {
	byte[] digest(byte[] text) throws Exception;
	boolean verify(byte[] text, byte[] digest) throws Exception;
}
