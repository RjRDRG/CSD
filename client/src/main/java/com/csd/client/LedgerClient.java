package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.csd.common.cryptography.hlib.HomoAdd;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.item.Resource;
import com.csd.common.item.Wallet;
import com.csd.common.request.*;
import com.csd.common.response.wrapper.Response;
import com.csd.common.util.Format;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Security;
import java.util.*;
import java.util.stream.Collectors;

import static com.csd.common.util.Conversion.doubleToBigInteger;
import static com.csd.common.util.Serialization.*;

@ActiveProfiles("ssl")
public class LedgerClient {

	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		System.setProperty("https.protocols", "TLSv1.2");
	}

	static String proxyIp = "localhost";
	public static String proxyPorts[] = {"8080","8081","8082","8083"};
	static int port = 0;

	static int endorsementQuorum = 3;

	static Map<String, Wallet> wallets = new HashMap<>();
	static Set<String> storedTransactions;

	public static void main(String[] args) throws Exception {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.setLevel(Level.toLevel("error"));

		createWallet("default@csd.com", UUID.randomUUID().toString(), null);

		storedTransactions = getFileNames("transactions");

		FlatDarculaLaf.setup();
		new LedgerSwingGUI();
	}

	static void createWallet(String id, String seed, IConsole console) {
		try {
			wallets.put(id,new Wallet(id,seed));
			if(console != null) {
				console.printOperation("createWallet: ", "Seed: " + seed);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static void changeProxy(String proxy) {
		port = ArrayUtils.indexOf(proxyPorts, proxy);
	}

	static void loadMoney(String walletId, double amount, IConsole console) {
		String requestString = "-----> loadMoney: " + walletId + " " + amount;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/load";

			Wallet wallet = wallets.get(walletId);

			LoadMoneyRequestBody request = new LoadMoneyRequestBody(
					wallet.clientId, wallet.signatureSuite, amount
			);

			ResponseEntity<Response<LoadMoneyRequestBody>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<LoadMoneyRequestBody>>() {});
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

			Wallet wallet = wallets.get(walletId);

			GetBalanceRequestBody request = new GetBalanceRequestBody(
					wallet.clientId, wallet.signatureSuite
			);

			ResponseEntity<Response<Double>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<Double>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}


	static void sendTransaction(String walletId, String walletDestinationId, double amount, double fee, IConsole console) {
		String requestString = "-----> Send Transaction: " + walletId + " " + walletDestinationId + " " + amount;
		String resultString;
		try {
			Wallet wallet = wallets.get(walletId);

			Wallet walletDestination = wallets.get(walletDestinationId);

			SendTransactionRequestBody request = new SendTransactionRequestBody(
					wallet.clientId, wallet.signatureSuite, walletDestination.clientId, amount, fee
			);

			int counter = 0;
			ResponseEntity<Response<SendTransactionRequestBody>> responseEntity = null;
			String uri;
			while (request.getProxySignatures().length < endorsementQuorum) {
				uri = "https://" + proxyIp + ":" + proxyPorts[counter] + "/transfer";
				responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<SendTransactionRequestBody>>() {});
				if (responseEntity.getBody().valid()) {
					request = responseEntity.getBody().getResponse();
				}
				counter++;
			}

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();

		} catch (Exception e) {
			resultString = Format.exception(e);
		}
		console.printOperation(requestString,resultString);
	}

	static String storeTransaction(String walletId, String walletDestinationId, double amount, double fee, IConsole console) {
		String requestString = "-----> Store Transaction: " + walletId + " " + walletDestinationId + " " + amount;
		String resultString;
		String filename = "";
		try {
			Wallet wallet = wallets.get(walletId);

			Wallet walletDestination = wallets.get(walletDestinationId);

			SendTransactionRequestBody request = new SendTransactionRequestBody(
					wallet.clientId, wallet.signatureSuite, walletDestination.clientId, amount, fee
			);

			File file = getUniqueFile("transactions", walletDestinationId.split("\\.")[0]);
			dataToJsonFile(file, request);

			filename = file.getName();

			storedTransactions.add(filename);

			resultString = file.getPath();
		} catch (Exception e) {
			resultString = Format.exception(e);
		}
		console.printOperation(requestString,resultString);
		return filename;
	}

	static void sendStoredTransaction(String walletId, String transaction, boolean isPrivate, IConsole console) {
		String requestString = "-----> Send Stored Transaction: " + walletId + " " + transaction;
		String resultString;
		try {
			Wallet wallet = wallets.get(walletId);


			SendTransactionRequestBody request = jsonFileToData(new File("transactions/"+transaction), SendTransactionRequestBody.class);

			if(isPrivate) {
				byte[] encryptedAmount = HomoAdd.encrypt(doubleToBigInteger(request.getAmount()),wallet.pk).toByteArray();
				request.encrypt(wallet.clientId, wallet.signatureSuite, encryptedAmount);
			}

			int counter = 0;
			ResponseEntity<Response<SendTransactionRequestBody>> responseEntity = null;
			String uri;
			while (request.getProxySignatures().length < endorsementQuorum) {
				uri = "https://" + proxyIp + ":" + proxyPorts[counter] + "/transfer";
				responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<SendTransactionRequestBody>>() {});
				if (responseEntity.getBody().valid()) {
					request = responseEntity.getBody().getResponse();
				}
				counter++;
			}

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();

		} catch (Exception e) {
			resultString = Format.exception(e);
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
			resultString = Format.exception(e);
		}
		console.printOperation(requestString,resultString);
	}

	static void getLedger(IConsole console) {
		String requestString = "-----> Get Ledger";
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/ledger";

			GetLedgerRequestBody request = new GetLedgerRequestBody();
			ResponseEntity<Response<ArrayList<Resource>>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<ArrayList<Resource>>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = Format.exception(e);
		}
		console.printOperation(requestString,resultString);
	}

	static void getExtract(String walletId, IConsole console) {
		String requestString = "-----> Get Extract: " + walletId;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/extract";

			Wallet wallet = wallets.get(walletId);

			GetExtractRequestBody request = new GetExtractRequestBody(
					wallet.clientId, wallet.signatureSuite
			);

			ResponseEntity<Response<ArrayList<Resource>>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<ArrayList<Resource>>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = Format.exception(e);
		}
		console.printOperation(requestString,resultString);
	}

	static void getTotalValue(List<String> walletsIds, IConsole console) {
		String requestString = "-----> Get Total Value: " + walletsIds;
		String resultString;
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/total";

			byte[][] clientId = new byte[walletsIds.size()][];
			SignatureSuite[] signatureSuite = new SignatureSuite[walletsIds.size()];

			int count = 0;
			for(String walletId : walletsIds ){
				Wallet wallet = wallets.get(walletId);
				clientId[count] = wallet.clientId;
				signatureSuite[count] = wallet.signatureSuite;
				count++;
			}

			GetTotalValueRequestBody request = new GetTotalValueRequestBody(clientId, signatureSuite);
			ResponseEntity<Response<Double>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<Double>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			resultString = Format.exception(e);
		}
		console.printOperation(requestString,resultString);
	}


	static MappingJackson2HttpMessageConverter createMappingJacksonHttpMessageConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(Serialization.jsonMapper);
		return converter;
	}

	static File getUniqueFile(String folderName, String searchedFilename) {
		int num = 1;
		File file = new File(folderName, searchedFilename);
		while (file.exists()) {
			String fileName = searchedFilename + "(" +(num++) + ")";
			file = new File(folderName, fileName);
		}
		return file;
	}

	static Set<String> getFileNames(String folderName) {
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		return Arrays.stream(folder.listFiles()).filter(File::isFile).map(File::getName).collect(Collectors.toSet());
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
					.loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
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
