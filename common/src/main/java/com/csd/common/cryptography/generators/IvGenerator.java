package com.csd.common.cryptography.generators;

import java.security.SecureRandom;

public class IvGenerator {

	private final int size;
	private final SecureRandom rand;
	
	public IvGenerator(int size) throws Exception{
		this.size = size;
		this.rand = new SecureRandom();
	}
	
	public byte[] generate() {
		byte[] iv = new byte[size];
		rand.nextBytes(iv);
		return iv;
	}
	
}
