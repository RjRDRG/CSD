package com.csd.common.cryptography.config;

import java.math.BigInteger;

public interface ISuiteSpecification {

	ISuiteSpecification getSubSpec(String id) throws Exception;
	
	boolean getBoolean(String id);

	int getInt(String id) throws Exception;
	
	float getFloat(String id) throws Exception;
	
	BigInteger getBigInteger(String id) throws Exception;
	
	String getString(String id) throws Exception;
	
	byte[] getBytes(String id) throws Exception;
}
