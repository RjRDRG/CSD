package com.csd.client;

import ch.qos.logback.classic.Level;
import com.csd.common.cryptography.config.ISuiteConfiguration;
import com.csd.common.cryptography.config.IniSpecification;
import com.csd.common.cryptography.config.StoredSecrets;
import com.csd.common.cryptography.config.SuiteConfiguration;
import com.csd.common.cryptography.key.EncodedPublicKey;
import com.csd.common.cryptography.key.KeyStoresInfo;
import com.csd.common.cryptography.suites.digest.FlexibleDigestSuite;
import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.item.RequestInfo;
import com.csd.common.request.GetBalanceRequestBody;
import com.csd.common.request.LoadMoneyRequestBody;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.traits.Seal;
import com.csd.common.traits.UniqueSeal;
import com.csd.common.util.Serialization;
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

import static com.csd.common.util.Serialization.bytesToString;

@ActiveProfiles("ssl")
public class LedgerClient {

	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	static final String SECURITY_CONF = "security.conf";

	static String proxyIp = "localhost";
	static String proxyPort = "8080";

	static class ClientDetails {
		public final byte[] clientId;
		public final EncodedPublicKey clientPublicKey;
		public final SignatureSuite signatureSuite;
		public long requestCounter;

		public ClientDetails() throws Exception {
			ISuiteConfiguration clientIdSuiteConfiguration =
					new SuiteConfiguration(
							new IniSpecification("client_id_digest_suite", SECURITY_CONF),
							new StoredSecrets(new KeyStoresInfo("stores", SECURITY_CONF))
					);
			FlexibleDigestSuite clientIdDigestSuite = new FlexibleDigestSuite(clientIdSuiteConfiguration, SignatureSuite.Mode.Digest);

			this.signatureSuite = new SignatureSuite(
					new IniSpecification("client_signature_suite", SECURITY_CONF),
					new IniSpecification("client_signature_keygen_suite", SECURITY_CONF)
			);
			this.clientPublicKey = signatureSuite.getPublicKey();

			this.clientId = clientIdDigestSuite.digest(clientPublicKey.getEncoded());

			this.requestCounter = 0;
		}

		String getUrlSafeClientId() {
			return bytesToString(clientId);
		}

		long getRequestCounter() {
			return ++requestCounter;
		}
	}

	static String manualToString(){
		return "Available operations : \n" +
				"h - Help;                                             Eg: h \n"+
				"w - List wallets ids;                                 Eg: w \n"+
				"O - Set the proxy port;                               Eg: 0 {8080, 8081, 8082, 8083} \n" +
				"1 - Create wallet;                                    Eg: 1 {wallet_id} \n" +
				"a - Obtain tokens;                                    Eg: a {wallet_id} {amount}\n" +
				"c - Consult balance of a certain client;              Eg: c {wallet_id}\n" +
				"z - Exit                                              Eg: z";
	}

	static Map<String, ClientDetails> clients = new HashMap<>();

	public static void main(String[] args) throws Exception {

		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		logger.setLevel(Level.toLevel("error"));

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.println(manualToString());

		while(true) {

			try {
				String[] command = in.readLine().split(" ");
				char op = command[0].charAt(0);
				switch (op) {
					case 'h':
						System.out.println(manualToString());
						break;
					case 'w':
						System.out.println(clients.keySet());
						break;
					case '0':
						proxyPort = command[1];
						break;
					case '1':
						clients.put(command[1], new ClientDetails());
						break;
					case 'a':
						loadMoney(command[1], Integer.parseInt(command[2]));
						break;
					case 'c':
						getBalance(command[1]);
						break;
					case 'z':
						return;
					default:
						System.out.println("Chosen operation does not exist. Please try again.");
						break;
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	static void loadMoney(String walletId, int amount) {
		String uri = "https://" + proxyIp + ":" + proxyPort + "/load";

		try {
			ClientDetails clientDetails = clients.get(walletId);

			UniqueSeal<LoadMoneyRequestBody> requestBody = new UniqueSeal<>(
					new LoadMoneyRequestBody(amount), clientDetails.getRequestCounter(), clientDetails.signatureSuite
			);
			ProtectedRequest<LoadMoneyRequestBody> request = new ProtectedRequest<>(clientDetails.clientId, clientDetails.clientPublicKey, requestBody);

			ResponseEntity<RequestInfo> requestInfo = restTemplate().postForEntity(uri, request, RequestInfo.class);

			System.out.println(requestInfo.getBody());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	static void getBalance(String walletId){
		String uri = "https://" + proxyIp + ":" + proxyPort + "/balance";

		try {
			ClientDetails clientDetails = clients.get(walletId);

			Seal<GetBalanceRequestBody> requestBody = new Seal<>(
					new GetBalanceRequestBody(""), clientDetails.signatureSuite
			);
			AuthenticatedRequest<GetBalanceRequestBody> request = new AuthenticatedRequest<>(clientDetails.clientId, clientDetails.clientPublicKey, requestBody);

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
