package com.csd.common.cryptography.generators.nonce;

import java.io.Serializable;

public interface INonce extends Serializable {
	INonce increment();
	boolean prior(INonce other);
	String toString();
	String type();
}
