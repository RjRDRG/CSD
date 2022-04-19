package com.csd.common.cryptography.generators;

import java.security.SecureRandom;
import java.util.Random;

public class StringGenerator {

	public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String LOWER = UPPER.toLowerCase();
	public static final String DIGITS = "0123456789";
	public static final char[] SYMBOLS = (UPPER + LOWER + DIGITS).toCharArray();

	private final int maxLenght;
	private final Random random;

	public StringGenerator(int maxLenght) {
		if (maxLenght < 1)
			throw new IllegalArgumentException();
		this.maxLenght = maxLenght;
		this.random = new SecureRandom();
	}
	
	public String nextString() {
		char[] buf  = new char[random.nextInt(maxLenght)];
		for (int i = 0; i < buf.length; ++i)
			buf[i] = SYMBOLS[random.nextInt(SYMBOLS.length)];
		return new String(buf);
	}

}
