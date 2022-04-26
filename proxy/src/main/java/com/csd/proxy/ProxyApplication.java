package com.csd.proxy;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class ProxyApplication {

	static {
		Security.addProvider(new BouncyCastleProvider());
		System.setProperty("https.protocols", "TLSv1.2");
	}

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

}
