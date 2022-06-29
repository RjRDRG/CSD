package com.csd.common.request;

import com.csd.common.cryptography.suites.digest.SignatureSuite;
import com.csd.common.traits.Signature;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import static com.csd.common.util.Serialization.concat;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class GetTotalValueRequestBody extends Request {

    public GetTotalValueRequestBody(byte[][] clientId, SignatureSuite[] signatureSuite) {
        try {
            this.requestId = UUID.randomUUID().toString();
            this.clientId = clientId;
            this.nonce = OffsetDateTime.now();
            this.clientSignature = new Signature[signatureSuite.length];
            int count = 0;
            for (SignatureSuite suite : signatureSuite) {
                clientSignature[count] = new Signature(suite.getPublicKey(), suite.digest(serializedRequest()));
                count++;
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetTotalValueRequestBody() {
    }

    @Override
    public byte[] serializedRequest() {
        return concat(dataToBytesDeterministic(requestId), concat(clientId),dataToBytesDeterministic(nonce));
    }

    @Override
    public String toString() {
        return "GetTotalValueRequestBody{" +
                "clientId=" + Arrays.toString(clientId) +
                ", clientSignature=" + Arrays.toString(clientSignature) +
                ", proxySignatures=" + Arrays.toString(proxySignatures) +
                ", nonce=" + nonce +
                '}';
    }
}
