package com.csd.proxy.impl;

import bftsmart.tom.ServiceProxy;
import com.csd.common.reply.ConsentedReply;
import com.csd.common.request.IRequest;
import com.csd.common.request.wrapper.ConsensualRequest;
import com.csd.common.traits.Result;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static com.csd.common.util.Serialization.dataToBytes;
import static com.csd.common.util.Serialization.bytesToData;

@Component
public class LedgerProxy extends ServiceProxy {

    public LedgerProxy(Environment environment) {
        super(environment.getProperty("proxy.id", Integer.class));
    }

    @SuppressWarnings("unchecked")
    public <R extends IRequest, T extends Serializable> Result<T> invokeUnordered(R request) {
        try {
            ConsensualRequest consensualRequest = new ConsensualRequest(request, 0);

            byte[] reply = super.invokeUnordered(dataToBytes(consensualRequest));
            if(reply.length == 0)
                return Result.error(Result.Status.NOT_AVAILABLE, "Not enough correct replicas");

            ConsentedReply consentedReply = bytesToData(reply);
            return consentedReply.extractReply();
        } catch (Exception e) {
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public <R extends IRequest, T extends Serializable> Result<T> invokeOrdered(R request) {
        try {
            ConsensualRequest consensualRequest = new ConsensualRequest(request, 0);

            byte[] reply = super.invokeOrdered(dataToBytes(consensualRequest));
            if(reply.length == 0)
                return Result.error(Result.Status.NOT_AVAILABLE, "Not enough correct replicas");

            ConsentedReply consentedReply = bytesToData(reply);
            return consentedReply.extractReply();
        } catch (Exception e) {
            return Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

}

