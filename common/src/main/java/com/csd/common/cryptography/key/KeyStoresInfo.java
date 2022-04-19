package com.csd.common.cryptography.key;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class KeyStoresInfo {

	public final char[] password;
	public final String keystoreType;
	public final String keystorePath;
	public final String keyManagerFactoryAlg;
	public final String truststoreType;
	public final String truststorePath;
	public final String trustManagerFactoryAlg;

	public KeyStoresInfo(String section, String configPath) throws Exception {
		File iniFile = new File(configPath);
		Ini ini = new Ini();
		ini.load(new FileInputStream(iniFile));
		
		Section stores = ini.get(section);

		String pass = stores.get("password");
		password = pass == null || pass.equals("N/A") ? askPassword() : pass.toCharArray();
		keystoreType = stores.get("keystoreType");
		keystorePath = stores.get("keystorePath");
		keyManagerFactoryAlg = stores.get("keyManagerFactoryAlg");

		truststoreType = stores.get("truststoreType");
		truststorePath = stores.get("truststorePath");
		trustManagerFactoryAlg = stores.get("trustManagerFactoryAlg");
	}
	
	public KeyStoresInfo(String configPath) throws Exception {
		this("stores",configPath);
	}
	
	private char[] askPassword() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Password: ");
		char[] password = br.readLine().toCharArray();
		System.out.println();
		return password;
	}
}
