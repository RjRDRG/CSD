package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.csd.common.item.RequestInfo;
import com.csd.common.item.Transaction;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Seal;
import com.csd.common.traits.UniqueSeal;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.security.KeyStore;
import java.security.Security;
import java.time.OffsetDateTime;
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

	@Deprecated
	static void startSession(String walletId, IConsole console) {
		String requestString = "-----> Start Session: " + walletId;
		String resultString;
		try{
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/session";

			WalletDetails wallet = wallets.get(walletId);

			AuthenticatedRequest<StartSessionRequestBody> request = new AuthenticatedRequest<>(wallet.clientId, wallet.clientPublicKey, wallet.signatureSuite, new StartSessionRequestBody(OffsetDateTime.now()));

			ResponseEntity<Long> nonce = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, Long.class);
			wallets.get(walletId).nonce = nonce.getBody();

			resultString = "Session Started for wallet {" + walletId + "} starting with nonce {" + nonce.getBody() + "}";
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}

	static void loadMoney(String walletId, double amount, IConsole console) {
		String requestString = "-----> loadMoney: " + walletId + " " + amount;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/load";

			WalletDetails wallet = wallets.get(walletId);

			if(wallet.nonce == null) {
				startSession(walletId,console);
			}

			ProtectedRequest<LoadMoneyRequestBody> request = new ProtectedRequest<>(
					wallet.clientId, wallet.clientPublicKey, wallet.signatureSuite, wallet.getNonce(),
					new LoadMoneyRequestBody(amount)
			);

			ResponseEntity<RequestInfo> requestInfo = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, RequestInfo.class);

			resultString = Objects.requireNonNull(requestInfo.getBody()).toString();
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

			if(wallet.nonce == null) {
				startSession(walletId,console);
			}

			AuthenticatedRequest<GetBalanceRequestBody> request = new AuthenticatedRequest<>(
					wallet.clientId, wallet.clientPublicKey, wallet.signatureSuite,
					new GetBalanceRequestBody()
			);

			ResponseEntity<Double> balance = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, Double.class);

			resultString = Objects.requireNonNull(balance.getBody()).toString();
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

			if(wallet.nonce == null) {
				startSession(walletId,console);
			}

			WalletDetails walletDestination = wallets.get(walletDestinationId);

			ProtectedRequest<SendTransactionRequestBody> request = new ProtectedRequest<>(
					wallet.clientId, wallet.clientPublicKey, wallet.signatureSuite, wallet.getNonce(),
					new SendTransactionRequestBody(walletDestination.clientId, amount)
			);

			ResponseEntity<RequestInfo> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, RequestInfo.class);

			resultString = Objects.requireNonNull(info.getBody()).toString();
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

			ResponseEntity<Double> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, new GetGlobalValueRequestBody(), Double.class);

			resultString = Objects.requireNonNull(info.getBody()).toString();
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

			ResponseEntity<Transaction[]> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, new GetLedgerRequestBody(), Transaction[].class);

			resultString = Arrays.toString(info.getBody());
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

			if(wallet.nonce == null) {
				startSession(walletId,console);
			}

			AuthenticatedRequest<GetExtractRequestBody> request = new AuthenticatedRequest<>(
					wallet.clientId, wallet.clientPublicKey, wallet.signatureSuite,
					new GetExtractRequestBody()
			);

			ResponseEntity<Transaction[]> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, Transaction[].class);

			resultString = Arrays.toString(Objects.requireNonNull(info.getBody()));
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
			ArrayList<AuthenticatedRequest<IRequest.Void>> walletList = new ArrayList<>(walletsIds.size());

			for(String walletId : walletsIds ){
				WalletDetails wallet = wallets.get(walletId);

				if(wallet.nonce == null) {
					startSession(walletId,console);
				}

				AuthenticatedRequest<IRequest.Void> request = new AuthenticatedRequest<>(
						wallet.clientId, wallet.clientPublicKey, wallet.signatureSuite,
						new IRequest.Void()
				);

				walletList.add(request);
			}

			GetTotalValueRequestBody request = new GetTotalValueRequestBody(walletList);
			ResponseEntity<Double> info = Objects.requireNonNull(restTemplate()).postForEntity(uri, request, Double.class);

			resultString = Objects.requireNonNull(info.getBody()).toString();
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
