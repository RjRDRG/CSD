package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class GetTotalValueRequestBody extends Request {

    public GetTotalValueRequestBody(byte[][] clientId, SignatureSuite[] signatureSuite) {
        try {
            this.requestId = UUID.randomUUID().toString();
            this.nonce = OffsetDateTime.now();
            this.clientId = new HashMap<>();
            this.clientSignature = new HashMap<>();
            for (int i=0; i<clientId.length; i++) {
                this.clientId.put(i,clientId[i]);
            }
            for (int i=0; i<clientId.length; i++) {
                SignatureSuite suite = signatureSuite[i];
                this.clientSignature.put(i,new Signature(suite.getPublicKey(), suite.digest(serializedRequest())));
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetTotalValueRequestBody() {
    }

    @Override
    public byte[] serializedRequest() {
        return concat(dataToBytesDeterministic(requestId), dataToBytesDeterministic(clientId), dataToBytesDeterministic(nonce));
    }

    @Override
    public String toString() {
        return "GetTotalValueRequestBody{" +
                "requestId='" + requestId + '\'' +
                ", clientId=" + clientId +
                ", clientSignature=" + clientSignature +
                ", proxySignatures=" + proxySignatures +
                ", nonce=" + nonce +
                '}';
    }
}
