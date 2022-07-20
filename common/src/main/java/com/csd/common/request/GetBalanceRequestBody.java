package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class GetBalanceRequestBody extends Request {

    public GetBalanceRequestBody(byte[] clientId, SignatureSuite signatureSuite) {
        try {
            this.requestId = UUID.randomUUID().toString();
            this.clientId = new HashMap<>();
            this.clientId.put(0,clientId);
            this.nonce = OffsetDateTime.now();
            this.clientSignature = new HashMap<>();
            this.clientSignature.put(0, new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest())));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetBalanceRequestBody() {
    }

    @Override
    public byte[] serializedRequest() {
        return concat(dataToBytesDeterministic(requestId), clientId.get(0), dataToBytesDeterministic(nonce));
    }

    @Override
    public String toString() {
        return "GetBalanceRequestBody{" +
                "clientId=" + clientId.get(0) +
                ", clientSignature=" + clientSignature.get(0) +
                ", proxySignatures=" + proxySignatures +
                ", nonce=" + nonce +
                '}';
    }
}
