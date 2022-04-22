package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.csd.common.item.RequestInfo;
import com.csd.common.request.GetBalanceRequestBody;
import com.csd.common.request.LoadMoneyRequestBody;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Seal;
import com.csd.common.traits.UniqueSeal;
import com.csd.common.util.Serialization;
import com.formdev.flatlaf.FlatDarculaLaf;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;


import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.security.KeyStore;
import java.security.Security;
import java.util.*;

@ActiveProfiles("ssl")
public class LedgerClient {

	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	static String proxyIp = "localhost";
	static String proxyPort = "8080";
	static Map<String, WalletDetails> wallets = new HashMap<>();

	public static void main(String[] args) {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.setLevel(Level.toLevel("error"));
		//new LedgerPrompt();
		FlatDarculaLaf.setup();
		new LedgerSwingGUI();
	}

	static void loadMoney(String walletId, int amount) {
		String uri = "https://" + proxyIp + ":" + proxyPort + "/load";

		try {
			WalletDetails wallet = wallets.get(walletId);

			UniqueSeal<LoadMoneyRequestBody> requestBody = new UniqueSeal<>(
					new LoadMoneyRequestBody(amount), wallet.getRequestCounter(), wallet.signatureSuite
			);
			ProtectedRequest<LoadMoneyRequestBody> request = new ProtectedRequest<>(wallet.clientId, wallet.clientPublicKey, requestBody);

			ResponseEntity<RequestInfo> requestInfo = restTemplate().postForEntity(uri, request, RequestInfo.class);

			System.out.println(requestInfo.getBody());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	static void getBalance(String walletId){
		String uri = "https://" + proxyIp + ":" + proxyPort + "/balance";

		try {
			WalletDetails wallet = wallets.get(walletId);

			Seal<GetBalanceRequestBody> requestBody = new Seal<>(
					new GetBalanceRequestBody(""), wallet.signatureSuite
			);
			AuthenticatedRequest<GetBalanceRequestBody> request = new AuthenticatedRequest<>(wallet.clientId, wallet.clientPublicKey, requestBody);

			ResponseEntity<Double> balance = restTemplate().postForEntity(uri, request, Double.class);

			System.out.println(balance.getBody());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	static MappingJackson2HttpMessageConverter createMappingJacksonHttpMessageConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(Serialization.jsonMapper);
		return converter;
	}

	static RestTemplate restTemplate() {
		try {
			RestTemplate restTemplate = new RestTemplate();

			restTemplate.getMessageConverters().add(0,createMappingJacksonHttpMessageConverter());

			KeyStore keyStore = KeyStore.getInstance("PKCS12");

			File keyFile = new File("keystore/csd.p12");
			FileSystemResource fileSystemResource = new FileSystemResource(keyFile);

			InputStream inputStream = fileSystemResource.getInputStream();
			keyStore.load(inputStream, "aq1sw2de3".toCharArray());

			SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder()
					.loadTrustMaterial(null, new TrustSelfSignedStrategy())
					.loadKeyMaterial(keyStore, "aq1sw2de3".toCharArray()).build(), NoopHostnameVerifier.INSTANCE);

			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

			restTemplate.setRequestFactory(requestFactory);

			return restTemplate;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
