package com.csd.common.cryptography.generators.key;


import com.csd.common.cryptography.config.ISuiteSpecification;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.crypto.spec.DHParameterSpec;
import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;

@SuppressWarnings("deprecation")
public class AsymmetricKeyPairGenerator {
	
	KeyPairGenerator keyGen;
	String certificateSignatureAlg;

	public AsymmetricKeyPairGenerator(ISuiteSpecification spec) throws Exception {
		String alg = spec.getString("alg");
		String provider = spec.getString("provider");	
		
		if (provider != null)
			keyGen = KeyPairGenerator.getInstance(alg, provider);
		else
			keyGen = KeyPairGenerator.getInstance(alg);
		
		if(alg.contains("EC")) {
			String ellipticCurve = spec.getString("ellipticCurve");	
			ECGenParameterSpec ecSpec = new ECGenParameterSpec(ellipticCurve);
			keyGen.initialize(ecSpec, new SecureRandom());
		} else if(alg.contains("DH")) {
			BigInteger p = spec.getBigInteger("p");
			BigInteger g = spec.getBigInteger("g");
			DHParameterSpec dhSpec = new DHParameterSpec(p, g);
			keyGen.initialize(dhSpec);
		} else {
			int keyLength = spec.getInt("keyLength");
			keyGen.initialize(keyLength, new SecureRandom());
		}
		
		certificateSignatureAlg = spec.getString("certificateSignatureAlg"); 
	}

	public KeyPair generateKeyPair() {
		return keyGen.generateKeyPair();
	}
	
	public X509Certificate generateCertificate(KeyPair keyPair) throws Exception {

		// build a certificate generator
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		X500Principal dnName = new X500Principal("cn=example");

		// add some options
		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setSubjectDN(new X509Name("dc=name"));
		certGen.setIssuerDN(dnName);
		// yesterday
		certGen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
		// in 2 years
		certGen.setNotAfter(new Date(System.currentTimeMillis() + 2L * 365 * 24 * 60 * 60 * 1000));
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm(certificateSignatureAlg);
		certGen.addExtension(X509Extensions.ExtendedKeyUsage, true,
				new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping));

		// finally, sign the certificate with the private key of the same KeyPair
		return certGen.generate(keyPair.getPrivate());
	}
}
