package com.csd.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Serialization {

	public static <T extends Serializable> byte[] dataToBytes(T data) {
		return SerializationUtils.serialize(data);
	}

	public static <T extends Serializable> byte[] dataToBytesDeterministic(T data) {
		return dataToJson(data).getBytes(StandardCharsets.UTF_8);
	}

	public static <T extends Serializable> T bytesToData(byte[] bytes) {
		return SerializationUtils.deserialize(bytes);
	}

	public static byte[] stringToBytes(String string) {
		return Base64.getUrlDecoder().decode(string);
	}

	public static String bytesToString(byte[] data) {
		return Base64.getUrlEncoder().encodeToString(data);
	}

	public static long bytesToInt(byte[] bytes) {
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

	public static byte[] intToBytes(long value) {
		byte[] bytes = new byte[Integer.BYTES];
		ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putLong(value);
		return bytes;
	}

	public static String bytesToHex(byte[] bytes) {
		return Hex.toHexString(bytes);
	}

	public static byte[] hexToBytes(String hex) {
		return Hex.decode(hex);
	}

	public final static ObjectMapper jsonMapper = createObjectMapper();

	private static ObjectMapper createObjectMapper() {
		ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		om.registerModule(new JavaTimeModule());
		SimpleModule module = new SimpleModule();
		module.addSerializer(byte[].class, new ByteSerializer());
		module.addDeserializer(byte[].class, new ByteDeserializer());
		om.registerModule(module);
		return om;
	}

	public static <T> String dataToJson(T data) {
		try {
			return jsonMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static <T> T jsonToData(String s, Class<T> type) {
		try {
			return jsonMapper.readValue(s, type);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static <T> void dataToJsonFile(File file, T data) {
		try {
			jsonMapper.writeValue(file, data);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static <T> T jsonFileToData(File s, Class<T> type) {
		try {
			return jsonMapper.readValue(s, type);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static <T> T jsonToData(String s, TypeReference<T> type) {
		try {
			return jsonMapper.readValue(s, type);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static byte[] concat(byte[]... a) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			for (byte[] b : a)
				outputStream.write(b);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return outputStream.toByteArray();
	}
}
