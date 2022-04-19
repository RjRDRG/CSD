package com.csd.common.cryptography.config;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.Base64;

public class IniSpecification implements ISuiteSpecification {

	private final String iniPath;
	private final Section specs;

	public IniSpecification(final String section, final String iniPath) throws Exception {
		this.iniPath = iniPath;
		File iniFile = new File(this.iniPath);
		Ini ini = new Ini();
		ini.load(new FileInputStream(iniFile));
		Section iniSec = ini.get(section);
		this.specs = iniSec;
	}

	@Override
	public ISuiteSpecification getSubSpec(String id) throws Exception {
		String subSection = getString(id);
		return new IniSpecification(subSection, iniPath);
	}

	@Override
	public boolean getBoolean(String id) {
		return Boolean.parseBoolean(specs.get(id));
	}

	@Override
	public int getInt(String id) throws Exception {
		return Integer.parseInt(specs.get(id));
	}

	@Override
	public float getFloat(String id) throws Exception {
		return Float.parseFloat(specs.get(id));
	}

	@Override
	public BigInteger getBigInteger(String id) throws Exception {
		return new BigInteger(specs.get(id));
	}

	@Override
	public String getString(String id) {
		return specs.get(id);
	}

	@Override
	public byte[] getBytes(String id) throws Exception {
		return Base64.getUrlDecoder().decode(specs.get(id));
	}
}
