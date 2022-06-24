package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class GetBalanceRequestBody extends Request {

    public GetBalanceRequestBody(byte[] clientId, SignatureSuite signatureSuite) {
        try {
            this.clientId = new byte[][]{clientId};
            this.clientSignature = new Signature[]{
                    new Signature(signatureSuite.getPublicKey(), signatureSuite.digest(serializedRequest()))
            };
            this.nonce = OffsetDateTime.now();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetBalanceRequestBody() {
    }

    @Override
    public byte[] serializedRequest() {
        return concat(clientId[0], dataToBytesDeterministic(nonce));
    }

    @Override
    public String toString() {
        return "GetBalanceRequestBody{" +
                "clientId=" + clientId[0] +
                ", clientSignature=" + clientSignature[0] +
                ", proxySignatures=" + Arrays.toString(proxySignatures) +
                ", nonce=" + nonce +
                '}';
    }
}
