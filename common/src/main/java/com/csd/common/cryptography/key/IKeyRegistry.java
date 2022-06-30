package com.csd.common.cryptography.key;

import java.security.PrivateKey;

public interface IKeyRegistry {
    EncodedPublicKey getReplicaKey(int id);
    EncodedPublicKey getProxyKey(int id);

    PrivateKey getProxyPrivateKey(int id);
}
