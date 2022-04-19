package com.csd.common.cryptography.generators;

import java.security.SecureRandom;

public class SaltGenerator {
	private int lenght;
	private SecureRandom random;
	public SaltGenerator(int lenght) {
		super();
		this.lenght = lenght;
		this.random = new SecureRandom();
	}
	
	public byte[] generate() {
		byte[] salt = new byte[lenght];
		random.nextBytes(salt);
		return salt;
	}
}
