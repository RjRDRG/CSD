package com.csd.common.response.wrapper;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;
import com.csd.common.util.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface Response<T extends Serializable> extends Serializable {
    boolean valid();

    T response();

    Status error();

    String message();

    Signature proxySignature();

    void proxySignature(SignatureSuite signatureSuite);

    void replicaSignatures(List<Signature> signatures);

    List<Signature> replicaSignatures();
}
