package com.csd.common.traits;

import java.io.Serializable;

/**
 * 
 * Represents the result of an operation, either wrapping a result of the given type,
 * or an error.
 * 
 * @author smd
 *
 * @param <T> type of the result value associated with success
 */
public interface Result<T> extends Serializable {

	enum Status{ OK, CONFLICT, NOT_FOUND, BAD_REQUEST, FORBIDDEN, INTERNAL_ERROR, NOT_IMPLEMENTED, NOT_AVAILABLE };

	/**
	 * Tests if the result is an error.
	 */
	boolean isOK();
	
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

	@Override
	String toString();

	/**
	 * Convenience method for returning non error results of the given type
	 * @param result of value of the result
	 * @return the value of the result
	 */
	static <T> Result<T> ok(T result) {
		return new OkResult<>(result);
	}

	/**
	 * Convenience method for returning non error results without a value
	 * @return non-error result
	 */
	static <T> OkResult<T> ok() {
		return new OkResult<>(null);	
	}
	
	/**
	 * Convenience method used to return an error 
	 * @return
	 */
	static <T> ErrorResult<T> error(Status error, String message) {
		return new ErrorResult<>(error,message);		
	}
	
	/**
	 * Convenience method used to return an error 
	 * @return
	 */
	static <T> ErrorResult<T> error(Status error) {
		return new ErrorResult<>(error,"");		
	}
	
}

class OkResult<T> implements Result<T> {

	final T result;

	OkResult(T result) {
		this.result = result;
	}
	
	@Override
	public boolean isOK() {
		return true;
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
	public String toString() {
		return "OkResult{" +
				"result=" + result +
				'}';
	}
}

class ErrorResult<T> implements Result<T> {

	final Status error;
	final String message;
	
	ErrorResult(Status error, String message) {
		this.error = error;
		this.message = message;
	}

	@Override
	public boolean isOK() {
		return false;
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
	public String toString() {
		return "ErrorResult{" +
				"error=" + error +
				", message='" + message + '\'' +
				'}';
	}
}