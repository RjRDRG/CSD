package com.csd.common.cryptography.key;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;

public class ExperimentalKeyRegistry implements IKeyRegistry {

    private static final String DEFAULT_UKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAokZC75w2IQLEyAgCpQqCDH3keTdHq+3lFOZJPbAev4zq73umOB3bFdSVu0OpbTwV7Mo7CHGTrtB4oi/REvgL6xwL/DKJ7Y2/cAQ91l4ApgmtyX6d0ESsVWZzCg57zjaiwHzzVN57R8q4/h3CcUxjDmCQtC9F4W83wm/sFvaTBovbkVQK5y2wBiQ3m+nFA9YWz+dgZy7wh4NJNbvnMpfhTBs73P64De6i2D/v2bjNJoke1mdSTM2+K9aSpwKBEedtI/mkQqQvA/eCAPNNDidXAVCewfHONpRu4wc/ovjPG+6AlrqRSEYy+GtAndgyPFc8L+VXAMdAyIe8109gTz4+lwIDAQAB";
    private static String DEFAULT_PKEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCiRkLvnDYhAsTICAKlCoIMfeR5N0er7eUU5kk9sB6/jOrve6Y4HdsV1JW7Q6ltPBXsyjsIcZOu0HiiL9ES+AvrHAv8Montjb9wBD3WXgCmCa3Jfp3QRKxVZnMKDnvONqLAfPNU3ntHyrj+HcJxTGMOYJC0L0XhbzfCb+wW9pMGi9uRVArnLbAGJDeb6cUD1hbP52BnLvCHg0k1u+cyl+FMGzvc/rgN7qLYP+/ZuM0miR7WZ1JMzb4r1pKnAoER520j+aRCpC8D94IA800OJ1cBUJ7B8c42lG7jBz+i+M8b7oCWupFIRjL4a0Cd2DI8Vzwv5VcAx0DIh7zXT2BPPj6XAgMBAAECggEAWzGdIEbrbHW/3KITymgzWY3OPgjA8HAK00nvUwmM3hz1UoxshxDQNF55nvmRV1/y+lVUA5TAZ/ekM5Enr79SA1iJq7tDJAsK0Iqxray6NJUv4xKS4Z4WMxAWCkrFbMfrgr01ijZVlazpdXWH9l/1Mvk1mO0QGnaEIXMAfI8pZP2CXL3oEIv+uhbPytdkwlCFtCgwLpteTbmX806i5R2NaeMS8JwGpI8bSL143Lxl1nR0w7X9+5LyXEFuOJRdy/qsso1/fqxFG/CXg3GCTJwWGsFWS2jEF0NVRAj/OOVlNp2smbvTRRXN+eRjAbc06R4aVlW9GwkMq0xaX4DIzVhWwQKBgQDZp+dRTqLXHkvPrqhEQOrrFS2mKmjnD1vFn1hBCGcezRfVcdY2zPumZvmFvUvdcHB+sOo7UHCTzKLeu61eI26qDmkSuATiyZuwp+rzjRzq9+lzdyq1ikP5XiCg/W5u0fXLFIbQ05o+T6Mp0DDXllKh0tKRFTP18FP3qcNEB3GwcQKBgQC+3LPo+EFXECZMTnZYnYOd6c+0KCWFgbgtbqsBUe6uZe5jiwrCq8ypAWcZpYBlEEnX0lZVfavSZLx+CUJE6zcnuV4B9qPjCEJMS1UTMDo2DPAbiUlNo29i/hgZ30UhhcxeNcDxyV4LCwzeVXjSGoj6lpH912ckgE76aJf+izbjhwKBgADGGQtv9SJAqFJXs59yf6NQUvY5RmAz7MaaF207w2oXnpMSsYlGV0qzKQXxs1hZMv0wUdTeJ1hPLPEPx8EC7TzLilIXt2S7BMOvBSXShZzMPtc7QDqfADjdvc30uLTKKE6NhyEs72pzTAg1Bkdt8GNE5ZzAb4vbS+EgiGIwiJXxAoGBAI7kGwl6+ygcXh/Yyj9zxbru9mKRf+3g/St+ZCZ72a7Vf1ElIqw2BOYut6p6vpJrTG14+svMZ1v/sSLG+ccxNjzWSaw8o9vwLfqCl7Hi9GHM8+IZuTiX+Gdrhk2wW0hKrIOHyOj78h1ga4T1Bpx94zmAitI5du3b5cURk2Gthi13AoGAYj6ygbie5vGSEi68/6dB7whHLsk2aqL9/8qe5CDGCPy43YhDXpHrJokHQ4xh3HR9QZuoEqV9oOYREVGjXYx4P1nUqKNMrbzXoypPczx+hNO22rJcu/flMmXGeC9q7tBbnuT6c8vKo3RLvwp7KA+fDYCMsfbeBa66hOVweG262hM=";

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

    @Override
    public PrivateKey getProxyPrivateKey(int id) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(DEFAULT_PKEY));
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
