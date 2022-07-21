package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.csd.common.cryptography.hlib.HomoAdd;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.item.Resource;
import com.csd.common.item.ValueToken;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigInteger;
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

	static int endorsementQuorum = (proxyPorts.length/2)+1;

	static Map<String, Wallet> wallets = new HashMap<>();
	static Set<String> storedTransactions = getFileNames("transactions");;

	static Map<String, ValueToken> tokens = new HashMap<>();;

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

	static long getBlock() {
		try {
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/block";
			ResponseEntity<Long> responseEntity = restTemplate().getForEntity(uri, Long.class);
			return responseEntity.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	static double getBalance(String walletId, IConsole console) {
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
			console.printOperation(requestString,resultString);
			return responseEntity.getBody().getResponse();
		} catch (Exception e) {
			e.printStackTrace();
			resultString = e.getMessage();
			console.printOperation(requestString,resultString);
			return 0;
		}
	}

	static void getEncryptedBalance(String walletId, IConsole console) {
		String requestString = "-----> Get Encrypted Balance: " + walletId;
		String resultString;
		try{
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/balance/encrypted";

			Wallet wallet = wallets.get(walletId);

			GetEncryptedBalanceRequestBody request = new GetEncryptedBalanceRequestBody(
					wallet.pk.getNsquare().toByteArray(), wallet.clientId, wallet.signatureSuite
			);

			ResponseEntity<Response<byte[]>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<byte[]>>() {});

			BigInteger balance = HomoAdd.decrypt(new BigInteger(responseEntity.getBody().getResponse()),wallet.pk);

			resultString = balance.toString();
		} catch (Exception e) {
			resultString = e.getMessage();
		}
		console.printOperation(requestString,resultString);
	}

	static void decryptValueAsset(String walletId, String asset, double fee, IConsole console) {
		String requestString = "-----> Decrypt Value Asset: " + walletId;
		String resultString;
		try{
			String uri = "https://" + proxyIp + ":" + proxyPorts[port] + "/decrypt";

			Wallet wallet = wallets.get(walletId);

			DecryptValueAssetRequestBody request = new DecryptValueAssetRequestBody(
					wallet.clientId, wallet.signatureSuite, tokens.get(asset), fee
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
		StringBuilder resultString = new StringBuilder();
		try {
			Wallet wallet = wallets.get(walletId);

			Wallet walletDestination = wallets.get(walletDestinationId);

			SendTransactionRequestBody request = new SendTransactionRequestBody(
					wallet.clientId, wallet.signatureSuite, walletDestination.clientId, amount, fee
			);

			ResponseEntity<Response<SendTransactionRequestBody>> responseEntity = null;
			for (int counter = 0; request.getProxySignatures().size() < endorsementQuorum && counter<proxyPorts.length; counter++) {
				String uri = "https://" + proxyIp + ":" + proxyPorts[counter] + "/transfer";
				try {
					responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<SendTransactionRequestBody>>() {});
				} catch (Exception e) {
					resultString.append("DENY: ").append(proxyPorts[counter]).append(" ").append(e.getMessage()).append("\n");
					continue;
				}
				request = responseEntity.getBody().getResponse();
			}

			if(responseEntity!=null)
				resultString.append(responseEntity.getBody());
		} catch (Exception e) {
			resultString = new StringBuilder(Format.exception(e));
		}
		console.printOperation(requestString, resultString.toString());
	}

	static void sendTransactionOnce(String walletId, String walletDestinationId, double amount, double fee, IConsole console) {
		String requestString = "-----> Send Transaction: " + walletId + " " + walletDestinationId + " " + amount;
		String resultString;
		try {
			Wallet wallet = wallets.get(walletId);

			Wallet walletDestination = wallets.get(walletDestinationId);

			SendTransactionRequestBody request = new SendTransactionRequestBody(
					wallet.clientId, wallet.signatureSuite, walletDestination.clientId, amount, fee
			);

			String uri = "https://" + proxyIp + ":" + proxyPorts[0] + "/transfer/once";
			ResponseEntity<Response<SendTransactionRequestBody>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<SendTransactionRequestBody>>() {});

			if(!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				System.err.println(responseEntity.getStatusCode() + ": " + responseEntity.getBody());
			}

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();

		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
			resultString = Format.exception(e);
		}
		console.printOperation(requestString,resultString);
		return filename;
	}

	static String sendStoredTransaction(String walletId, String transaction, boolean isPrivate, IConsole console) {
		String requestString = "-----> Send Stored Transaction: " + walletId + " " + transaction;
		StringBuilder resultString = new StringBuilder();
		ValueToken token = null;
		try {
			Wallet wallet = wallets.get(walletId);

			SendTransactionRequestBody request = jsonFileToData(new File("transactions/"+transaction), SendTransactionRequestBody.class);

			if(isPrivate) {
				byte[] encryptedAmount = HomoAdd.encrypt(doubleToBigInteger(request.getAmount()),wallet.pk).toByteArray();
				request.encrypt(wallet.clientId, wallet.signatureSuite, encryptedAmount);
			}

			ResponseEntity<Response<SendTransactionRequestBody>> responseEntity = null;
			for (int counter = 0; request.getProxySignatures().size() < endorsementQuorum && counter<proxyPorts.length; counter++) {
				String uri = "https://" + proxyIp + ":" + proxyPorts[counter] + "/transfer";
				try {
					responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<SendTransactionRequestBody>>() {});
				} catch (Exception e) {
					resultString.append("DENY: ").append(proxyPorts[counter]).append(" ").append(e.getMessage()).append("\n");
					continue;
				}
				request = responseEntity.getBody().getResponse();
			}

			if(responseEntity != null) {
				resultString.append(responseEntity.getBody());
				if(isPrivate) {
					Response<SendTransactionRequestBody> response = responseEntity.getBody();
					token = new ValueToken(
							response.getResponse().getRequestId(),
							response.getResponse().getEncryptedAmount(),
							response.getResponse().getAmount(),
							response.getReplicaResponses()
					);
					tokens.put(token.getPrivateValueAsset().getAsset(),token);
				}
			}

		} catch (Exception e) {
			resultString = new StringBuilder(Format.exception(e));
		}

		console.printOperation(requestString, resultString.toString());
		if (token != null)
			return token.getPrivateValueAsset().getAsset();
		else
			return null;
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
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
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
			System.out.println(dataToJson(request));
			ResponseEntity<Response<Double>> responseEntity = restTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<Response<Double>>() {});

			resultString = Objects.requireNonNull(responseEntity.getBody()).toString();
		} catch (Exception e) {
			e.printStackTrace();
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
