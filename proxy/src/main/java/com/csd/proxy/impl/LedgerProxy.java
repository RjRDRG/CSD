package com.csd.proxy.impl;

import bftsmart.tom.ServiceProxy;
import com.csd.common.reply.ConsentedReply;
import com.csd.common.request.wrapper.ConsensualRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static com.csd.common.util.Serialization.dataToBytes;
import static com.csd.common.util.Serialization.bytesToData;

@Component
public class LedgerProxy extends ServiceProxy {

    public LedgerProxy(Environment environment) {
        super(environment.getProperty("proxy.id", Integer.class),
                null,
                null,
                null,
                null
        );
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable, R extends Serializable> ConsentedReply<R> invokeUnordered(ConsensualRequest<T> request) {
        byte[] reply = super.invokeUnordered(dataToBytes(request));
        return bytesToData(reply);
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable, R extends Serializable> ConsentedReply<R> invokeOrdered(ConsensualRequest<T> request) {
        byte[] reply = super.invokeOrdered(dataToBytes(request));
        return bytesToData(reply);
    }

}

