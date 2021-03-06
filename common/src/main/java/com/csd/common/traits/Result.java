package com.csd.common.traits;

import com.csd.common.util.Status;

import java.io.Serializable;
import java.util.Objects;

import static com.csd.common.util.Serialization.dataToBytes;
import static com.csd.common.util.Serialization.dataToJson;

/**
 * 
 * Represents the result of an operation, either wrapping a result of the given type,
 * or an error.
 * 
 * @author smd
 *
 * @param <T> type of the result value associated with success
 */
public interface Result<T extends Serializable> extends Serializable {

	/**
	 * Tests if the result is an error.
	 */
	boolean valid();
	
	/**
	 * obtains the payload value of this result
	 * @return the value of this result.
	 */
	T value();

	/**
	 *
	 * obtains the error code of this result
	 * @return the error code
	 * 
	 */
	Status error();
	
	/**
	 *
	 * obtains the error message of this result
	 * @return the error message
	 * 
	 */
	String message();

	Result<byte[]> encode();

	@Override
	String toString();

	/**
	 * Convenience method for returning non error results of the given type
	 * @param result of value of the result
	 * @return the value of the result
	 */
	static <T extends Serializable> Result<T> ok(T result) {
		return new OkResult<>(result);
	}

	/**
	 * Convenience method for returning non error results without a value
	 * @return non-error result
	 */
	static <T extends Serializable> Result<T> ok() {
		return new OkResult<>(null);	
	}
	
	/**
	 * Convenience method used to return an error 
	 * @return
	 */
	static <T extends Serializable> Result<T> error(Status error, String message) {
		return new ErrorResult<>(error,message);		
	}

	static <T extends Serializable, E extends Serializable> Result<T> error(Result<E> error) {
		return new ErrorResult<>(error.error(), error.message());
	}
	
}

class OkResult<T extends Serializable> implements Result<T> {

	T result;

	OkResult(T result) {
		this.result = result;
	}

	public OkResult() {
	}

	public void setResult(T result) {
		this.result = result;
	}

	@Override
	public boolean valid() {
		return true;
	}

	public T getResult() {
		return result;
	}

	@Override
	public T value() {
		return result;
	}

	@Override
	public Status error() {
		return Status.OK;
	}
	
	@Override
	public String message() {
		return "";
	}

	@Override
	public Result<byte[]> encode() {
		return Result.ok(dataToBytes(result));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OkResult<?> okResult = (OkResult<?>) o;
		return result.equals(okResult.result);
	}

	@Override
	public int hashCode() {
		return Objects.hash(result);
	}

	@Override
	public String toString() {
		return dataToJson(result);
	}
}

class ErrorResult<T extends Serializable> implements Result<T> {

	Status error;
	String message;
	
	ErrorResult(Status error, String message) {
		this.error = error;
		this.message = message;
	}

	public ErrorResult() {
	}

	public void setError(Status error) {
		this.error = error;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean valid() {
		return false;
	}

	public Status getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public T value() {
		throw new RuntimeException("Attempting to extract the value of an Error: " + error());
	}

	@Override
	public Status error() {
		return error;
	}
	
	@Override
	public String message() {
		return message;
	}

	@Override
	public Result<byte[]> encode() {
		return Result.error(error,message);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ErrorResult<?> that = (ErrorResult<?>) o;
		return error == that.error && Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(error, message);
	}

	@Override
	public String toString() {
		return dataToJson(this);
	}
}