package com.csd.common.cryptography.generators.nonce;

public class Counter implements INonce {
	private static final long serialVersionUID = 4408482201985356841L;
	
	public static final String TYPE = "Counter";
	
	long counter;
	
	public Counter(long value) {
		this.counter = value;
	}
	
	public Counter(String value) {
		this.counter = Long.parseLong(value);
	}
	
	@Override
	public Counter increment() {
		return new Counter(counter+1);
	}

	@Override
	public boolean prior(INonce otherNonce) {
		if(!(otherNonce instanceof Counter))
			return false;
		else {
			Counter other = (Counter)otherNonce;
			return other.counter > this.counter;
		}
	}
	
	@Override
	public String type() {
		return TYPE;
	}
	
	@Override
	public String toString() {
		return Long.toString(counter);
	}
	
	public Counter() {
		this.counter = 0;
	}

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}
}
