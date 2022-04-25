package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.csd.common.item.RequestInfo;
import com.csd.common.item.Transaction;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Result;
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

	public static void main(String[] args) throws Exception {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.setLevel(Level.toLevel("error"));

		wallets.put("default",new WalletDetails());

		//new LedgerPrompt();
		FlatDarculaLaf.setup();
		new LedgerSwingGUI();
	}

	static void loadMoney(String walletId, double amount, IConsole console) {
		String requestString = "-----> loadMoney: " + walletId + " " + amount;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPort + "/load";

			WalletDetails wallet = wallets.get(walletId);

			UniqueSeal<LoadMoneyRequestBody> requestBody = new UniqueSeal<>(
					new LoadMoneyRequestBody(amount), wallet.getRequestCounter(), wallet.signatureSuite
			);
			ProtectedRequest<LoadMoneyRequestBody> request = new ProtectedRequest<>(wallet.clientId, wallet.clientPublicKey, requestBody);

			ResponseEntity<RequestInfo> requestInfo = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, RequestInfo.class);

			resultString = Objects.requireNonNull(requestInfo.getBody()).toString();
		} catch (Exception e) {
			resultString = Result.error(Result.Status.NOT_AVAILABLE, e.getClass().getSimpleName() + ": " + e.getMessage()).toString();
		}
		console.printOperation(requestString,resultString);
	}

	static void getBalance(String walletId, IConsole console) {
		String requestString = "-----> Get Balance: " + walletId;
		String resultString;
		try{
			String uri = "https://" + proxyIp + ":" + proxyPort + "/balance";

			WalletDetails wallet = wallets.get(walletId);

			Seal<GetBalanceRequestBody> requestBody = new Seal<>(
					new GetBalanceRequestBody(""), wallet.signatureSuite
			);
			AuthenticatedRequest<GetBalanceRequestBody> request = new AuthenticatedRequest<>(wallet.clientId, wallet.clientPublicKey, requestBody);

			ResponseEntity<Double> balance = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, Double.class);

			resultString = Objects.requireNonNull(balance.getBody()).toString();
		} catch (Exception e) {
			resultString =  Result.error(Result.Status.NOT_AVAILABLE, e.getClass().getSimpleName() + ": " + e.getMessage()).toString();
		}
		console.printOperation(requestString,resultString);
	}


	static void sendTransaction(String walletId, String walletDestinationId, double amount, IConsole console) {
		String requestString = "-----> Send Transaction: " + walletId + " " + walletDestinationId + " " + amount;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPort + "/transfer";

			WalletDetails wallet = wallets.get(walletId);
			WalletDetails walletDestination = wallets.get(walletDestinationId);

			UniqueSeal<SendTransactionRequestBody> requestBody = new UniqueSeal<>(
					new SendTransactionRequestBody(walletDestination.clientId, amount), 0, wallet.signatureSuite
			);
			ProtectedRequest<SendTransactionRequestBody> request = new ProtectedRequest<>(wallet.clientId, wallet.clientPublicKey, requestBody);

			ResponseEntity<RequestInfo> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, RequestInfo.class);

			resultString = Objects.requireNonNull(info.getBody()).toString();
		} catch (Exception e) {
			resultString = Result.error(Result.Status.NOT_AVAILABLE, e.getClass().getSimpleName() + ": " + e.getMessage()).toString();
		}
		console.printOperation(requestString,resultString);
	}

	static void getGlobalValue(IConsole console) {
		String requestString = "-----> Get Global Value";
		String resultString;
		try{
			String uri = "https://" + proxyIp + ":" + proxyPort + "/global";

			ResponseEntity<Double> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, new GetGlobalValueRequestBody(), Double.class);

			resultString = Objects.requireNonNull(info.getBody()).toString();
		} catch (Exception e) {
			resultString = Result.error(Result.Status.NOT_AVAILABLE, e.getClass().getSimpleName() + ": " + e.getMessage()).toString();
		}
		console.printOperation(requestString,resultString);
	}

	static void getLedger(IConsole console) {
		String requestString = "-----> Get Ledger";
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPort + "/ledger";

			ResponseEntity<Transaction[]> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, new GetLedgerRequestBody(), Transaction[].class);

			resultString = Arrays.toString(info.getBody());
		} catch (Exception e) {
			resultString = Result.error(Result.Status.NOT_AVAILABLE, e.getClass().getSimpleName() + ": " + e.getMessage()).toString();
		}
		console.printOperation(requestString,resultString);
	}

	static void getExtract(String walletId, IConsole console) {
		String requestString = "-----> Get Extract: " + walletId;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPort + "/extract";

			WalletDetails wallet = wallets.get(walletId);


			Seal<GetExtractRequestBody> requestBody = new Seal<>(
					new GetExtractRequestBody(), wallet.signatureSuite
			);
			AuthenticatedRequest<GetExtractRequestBody> request = new AuthenticatedRequest<>(wallet.clientId, wallet.clientPublicKey, requestBody);

			ResponseEntity<Transaction[]> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, Transaction[].class);

			resultString = Arrays.toString(Objects.requireNonNull(info.getBody()));
		} catch (Exception e) {
			resultString = Result.error(Result.Status.NOT_AVAILABLE, e.getClass().getSimpleName() + ": " + e.getMessage()).toString();
		}
		console.printOperation(requestString,resultString);
	}

	static void getTotalValue(List<String> walletsIds, IConsole console) {
		String requestString = "-----> Get Total Value: " + walletsIds;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPort + "/total";
			ArrayList<AuthenticatedRequest<IRequest.Void>> walletList = new ArrayList<>(walletsIds.size());

			for( String walletId : walletsIds ){
				WalletDetails wallet = wallets.get(walletId);
				Seal<IRequest.Void> requestBody = new Seal<>( //TODO IRequest.void has to have a field?
						new IRequest.Void(), wallet.signatureSuite
				);
				AuthenticatedRequest<IRequest.Void> request = new AuthenticatedRequest<>(wallet.clientId, wallet.clientPublicKey, requestBody);
				walletList.add(request);
			}

			GetTotalValueRequestBody request = new GetTotalValueRequestBody(walletList);
			ResponseEntity<Double> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, Double.class);

			resultString = Objects.requireNonNull(info.getBody()).toString();
		} catch (Exception e) {
			resultString = Result.error(Result.Status.NOT_AVAILABLE, e.getClass().getSimpleName() + ": " + e.getMessage()).toString();
		}
		console.printOperation(requestString,resultString);
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
