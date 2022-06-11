package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.csd.common.item.TransactionDetails;
import com.csd.common.item.Transaction;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.SignedRequest;
import com.csd.common.response.wrapper.Response;
import com.csd.common.util.Serialization;
import com.formdev.flatlaf.FlatDarculaLaf;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;


import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
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
		System.setProperty("https.protocols", "TLSv1.2");
	}

	static String proxyIp = "localhost";
	public static String proxyPorts[] = {"8080","8081","8082","8083"};
	static int port = 0;

	static Map<String, WalletDetails> wallets = new HashMap<>();

	public static void main(String[] args) throws Exception {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.setLevel(Level.toLevel("error"));

		wallets.put("default@csd.com",new WalletDetails("default@csd.com","0"));

		//new LedgerPrompt();
		FlatDarculaLaf.setup();
		new LedgerSwingGUI();
	}

	static void changeProxy(String proxy) {
		port = ArrayUtils.indexOf(proxyPorts, proxy);
	}

	static void loadMoney(String walletId, double amount, IConsole console) {
		String requestString = "-----> loadMoney: " + walletId + " " + amount;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/load";

			WalletDetails wallet = wallets.get(walletId);

			SignedRequest<LoadMoneyRequestBody> request = new SignedRequest<>(
					wallet.clientId, wallet.signatureSuite,
					new LoadMoneyRequestBody(amount)
			);

			ResponseEntity<Response<TransactionDetails>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<TransactionDetails>>() {});
			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}

	static void getBalance(String walletId, IConsole console) {
		String requestString = "-----> Get Balance: " + walletId;
		String resultString;
		try{
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/balance";

			WalletDetails wallet = wallets.get(walletId);

			SignedRequest<GetBalanceRequestBody> request = new SignedRequest<>(
					wallet.clientId, wallet.signatureSuite,
					new GetBalanceRequestBody()
			);

			ResponseEntity<Response<Double>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<Double>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}


	static void sendTransaction(String walletId, String walletDestinationId, double amount, IConsole console) {
		String requestString = "-----> Send Transaction: " + walletId + " " + walletDestinationId + " " + amount;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/transfer";

			WalletDetails wallet = wallets.get(walletId);

			WalletDetails walletDestination = wallets.get(walletDestinationId);

			SignedRequest<SendTransactionRequestBody> request = new SignedRequest<>(
					wallet.clientId, wallet.signatureSuite,
					new SendTransactionRequestBody(walletDestination.clientId, amount)
			);

			ResponseEntity<Response<TransactionDetails>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<TransactionDetails>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}

	static void getGlobalValue(IConsole console) {
		String requestString = "-----> Get Global Value";
		String resultString;
		try{
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/global";

			GetGlobalValueRequestBody request = new GetGlobalValueRequestBody();
			ResponseEntity<Response<Double>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<Double>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}

	static void getLedger(IConsole console) {
		String requestString = "-----> Get Ledger";
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/ledger";

			GetLedgerRequestBody request = new GetLedgerRequestBody();
			ResponseEntity<Response<ArrayList<Transaction>>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<ArrayList<Transaction>>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}

	static void getExtract(String walletId, IConsole console) {
		String requestString = "-----> Get Extract: " + walletId;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/extract";

			WalletDetails wallet = wallets.get(walletId);

			SignedRequest<GetExtractRequestBody> request = new SignedRequest<>(
					wallet.clientId, wallet.signatureSuite,
					new GetExtractRequestBody()
			);

			ResponseEntity<Response<ArrayList<Transaction>>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<ArrayList<Transaction>>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}

	static void getTotalValue(List<String> walletsIds, IConsole console) {
		String requestString = "-----> Get Total Value: " + walletsIds;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/total";
			ArrayList<SignedRequest<IRequest.Void>> walletList = new ArrayList<>(walletsIds.size());

			for(String walletId : walletsIds ){
				WalletDetails wallet = wallets.get(walletId);

				SignedRequest<IRequest.Void> request = new SignedRequest<>(
						wallet.clientId, wallet.signatureSuite,
						new IRequest.Void()
				);

				walletList.add(request);
			}

			GetTotalValueRequestBody request = new GetTotalValueRequestBody(walletList);
			ResponseEntity<Response<Double>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<Double>>() {});


			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}


	static MappingJackson2HttpMessageConverter createMappingJacksonHttpMessageConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(Serialization.jsonMapper);
		return converter;
	}

	static @NonNull RestTemplate restTemplate() {
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
			throw new RuntimeException(e);
		}
	}
}
