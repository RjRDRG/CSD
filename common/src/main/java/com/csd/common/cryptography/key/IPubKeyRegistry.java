package com.csd.common.cryptography.key;

public interface IPubKeyRegistry {
    EncodedPublicKey getReplicaKey(int id);
    EncodedPublicKey getProxyKey(int id);
}
