package com.csd.replica.impl;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.csd.common.item.RequestInfo;
import com.csd.common.reply.ConsentedReply;
import com.csd.common.request.GetBalanceRequestBody;
import com.csd.common.request.LoadMoneyRequestBody;
import com.csd.common.request.wrapper.AuthenticatedRequest;
import com.csd.common.request.wrapper.ConsensualRequest;
import com.csd.common.request.wrapper.ProtectedRequest;
import com.csd.common.item.Transaction;
import com.csd.common.traits.Result;
import com.csd.common.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;

import static com.csd.common.util.Serialization.bytesToData;
import static com.csd.common.util.Serialization.dataToBytes;

@Component
public class LedgerReplica extends DefaultSingleRecoverable {

    public static final String CONFIG_PATH = "security.conf";

    private static final Logger log = LoggerFactory.getLogger(LedgerReplica.class);

    private final Environment environment;

    private int replicaId;

    private final LedgerService ledgerService;

    public LedgerReplica(LedgerService ledgerService, Environment environment) throws Exception {
        this.ledgerService = ledgerService;
        this.environment = environment;
    }

    public void start(String[] args) {
        replicaId = args.length > 0 ? Integer.parseInt(args[0]) : environment.getProperty("replica.id", int.class);
        log.info("The id of the replica is: " + replicaId);
        new ServiceReplica(replicaId, this, this);
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {

        ConsensualRequest consensualRequest = Serialization.bytesToData(command);

        try {
            switch (consensualRequest.getOperation()) {
                case BALANCE: {
                    AuthenticatedRequest<GetBalanceRequestBody> request = consensualRequest.extractRequest();
                    Result<Double> result = ledgerService.getBalance(request);

                    return dataToBytes(new ConsentedReply(result.encode(), Collections.emptyList()));
                }
                default: {
                    Result<Serializable> result = Result.error(Result.Status.NOT_IMPLEMENTED, consensualRequest.getOperation().name());

                    return dataToBytes(new ConsentedReply(result.encode(), Collections.emptyList()));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());

            Result<Serializable> result = Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());

            return dataToBytes(new ConsentedReply(result.encode(), Collections.emptyList()));
        }
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {

        ConsensualRequest consensualRequest = bytesToData(command);

        try {
            switch (consensualRequest.getOperation()) {
                case LOAD: {
                    ProtectedRequest<LoadMoneyRequestBody> request = consensualRequest.extractRequest();
                    Result<RequestInfo> result = ledgerService.loadMoney(request, consensualRequest.getTimestamp());

                    return dataToBytes(new ConsentedReply(result.encode(), Collections.emptyList()));
                }
                default: {
                    Result<Serializable> result = Result.error(Result.Status.NOT_IMPLEMENTED, consensualRequest.getOperation().name());

                    return dataToBytes(new ConsentedReply(result.encode(), Collections.emptyList()));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());

            Result<Serializable> result = Result.error(Result.Status.INTERNAL_ERROR, e.getMessage());

            return dataToBytes(new ConsentedReply(result.encode(), Collections.emptyList()));
        }
    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public void installSnapshot(byte[] state) {
    }

}