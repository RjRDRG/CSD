package com.csd.common.cryptography.config;

public interface ISuiteConfiguration extends ISuiteSpecification, ISuiteSecrets {
	ISuiteConfiguration getSubConfiguration(String id) throws Exception;
}