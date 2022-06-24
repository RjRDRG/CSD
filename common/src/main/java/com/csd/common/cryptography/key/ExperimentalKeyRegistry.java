package com.csd.common.cryptography.key;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;

public class ExperimentalKeyRegistry implements IPubKeyRegistry {

    private static final String DEFAULT_UKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAokZC75w2IQLEyAgCpQqCDH3keTdHq+3lFOZJPbAev4zq73umOB3bFdSVu0OpbTwV7Mo7CHGTrtB4oi/REvgL6xwL/DKJ7Y2/cAQ91l4ApgmtyX6d0ESsVWZzCg57zjaiwHzzVN57R8q4/h3CcUxjDmCQtC9F4W83wm/sFvaTBovbkVQK5y2wBiQ3m+nFA9YWz+dgZy7wh4NJNbvnMpfhTBs73P64De6i2D/v2bjNJoke1mdSTM2+K9aSpwKBEedtI/mkQqQvA/eCAPNNDidXAVCewfHONpRu4wc/ovjPG+6AlrqRSEYy+GtAndgyPFc8L+VXAMdAyIe8109gTz4+lwIDAQAB";

    public ExperimentalKeyRegistry() {
    }

    public EncodedPublicKey getReplicaKey(int id) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(DEFAULT_UKEY));
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return new EncodedPublicKey(publicKey);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public EncodedPublicKey getProxyKey(int id){
        return null; //TODO
    }
}
