package com.csd.common.cryptography.key;

public class KeyInfo {
	
	public enum Type {Secret, Private, Public}
	
	String keyAlias;
	String alg;
	Integer keyLenght;
	Type type;

	public KeyInfo(String keyAlias, String alg, Integer keyLenght, Type type) {
		this.keyAlias = keyAlias;
		this.alg = alg;
		this.keyLenght = keyLenght;
		this.type = type;
	}

	KeyInfo() {
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}

	public String getAlg() {
		return alg;
	}

	public void setAlg(String alg) {
		this.alg = alg;
	}

	public Integer getKeyLenght() {
		return keyLenght;
	}

	public void setKeyLenght(Integer keyLenght) {
		this.keyLenght = keyLenght;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyAlias == null) ? 0 : keyAlias.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyInfo other = (KeyInfo) obj;
		if (keyAlias == null) {
			if (other.keyAlias != null)
				return false;
		} else if (!keyAlias.equals(other.keyAlias))
			return false;
		return true;
	}

}
