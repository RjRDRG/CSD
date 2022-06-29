package com.csd.replica.consensuslayer.blockmess;

import applicationInterface.ApplicationInterface;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.item.Resource;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.csd.common.traits.IConsensusLayer;
import com.csd.replica.servicelayer.ReplicaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;

import static com.csd.common.util.Serialization.bytesToData;
import static com.csd.common.util.Serialization.dataToBytes;

@Component
public class BlockmessOrderer extends ApplicationInterface implements IConsensusLayer {

    private static final Logger log = LoggerFactory.getLogger(BlockmessOrderer.class);

    private final ReplicaService replicaService;
    private final RequestValidator validator;

    public BlockmessOrderer(ReplicaService replicaService, Environment environment) throws Exception {
        super(new String[]{});
        this.replicaService = replicaService;
        this.validator = new RequestValidator(environment.getProperty("proxy.quorum.size" , int.class));
    }

    @Override
    public void start(String[] args) throws Exception {

    }

    @Override
    public byte[] processOperation(byte[] command) {
        try {
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            log.error(e.getMessage());
            Result<Serializable> result = Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
            return dataToBytes(new ConsensusResponse(result.encode(), new Resource[0]));
        }
    }

    public ConsensusResponse execute(ConsensusRequest consensusRequest) {
        switch (consensusRequest.getType()) {
            case TRANSFER: {
                SendTransactionRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, replicaService.getLastResourceDate(request.getClientId()[0]), true);
                Result<SendTransactionRequestBody> result =  v.valid() ? replicaService.sendTransaction(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
            default: {
                Result<Serializable> result = Result.error(Status.NOT_IMPLEMENTED, consensusRequest.getType().name());
                return new ConsensusResponse(result.encode(), replicaService.getResourcesAfterId(consensusRequest.getLastEntryId()));
            }
        }
    }
}
