package com.csd.proxy.impl;

import com.csd.common.request.Request;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.Response;

import java.io.Serializable;

public interface LedgerProxy {

    <R extends Request, T extends Serializable> Response<T> invokeUnordered(R request, ConsensusRequest.Type t0);

    @SuppressWarnings("unchecked")
    <R extends Request, T extends Serializable> Response<T> invokeOrdered(R request, ConsensusRequest.Type t0);

}
