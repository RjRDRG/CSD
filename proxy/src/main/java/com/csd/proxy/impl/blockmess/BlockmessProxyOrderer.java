package com.csd.proxy.impl.blockmess;

import applicationInterface.ApplicationInterface;
import com.csd.common.cryptography.validator.RequestValidator;
import com.csd.common.item.Resource;
import com.csd.common.request.*;
import com.csd.common.request.wrapper.ConsensusRequest;
import com.csd.common.response.wrapper.ConsensusResponse;
import com.csd.common.response.wrapper.Response;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.csd.common.traits.IConsensusLayer;
import com.csd.proxy.impl.LedgerProxy;
import com.csd.proxy.impl.pow.PowReplyListener;
import com.csd.proxy.ledger.ResourceEntity;
import com.csd.proxy.ledger.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.*;

@Component
public class BlockmessProxyOrderer extends ApplicationInterface implements IConsensusLayer, LedgerProxy {

    private static final Logger log = LoggerFactory.getLogger(BlockmessProxyOrderer.class);

    private static final int TIMEOUT_PERIOD = 5000;
    private final int quorum;
    private final RequestValidator validator;

    public final ResourceRepository resourceRepository;

    public BlockmessProxyOrderer(Environment environment, ResourceRepository resourceRepository) throws Exception {
        super(new String[]{});
        this.quorum = environment.getProperty("proxy.quorum.size" , int.class);
        this.validator = new RequestValidator(quorum);
        this.resourceRepository = resourceRepository;
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
                var v = validator.validate(request, getLastResourceDate(request.getClientId()[0]), true);
                Result<SendTransactionRequestBody> result =  v.valid() ? sendTransaction(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), null);
            }
            default: {
                Result<Serializable> result = Result.error(Status.NOT_IMPLEMENTED, consensusRequest.getType().name());
                return new ConsensusResponse(result.encode(), null);
            }
        }
    }

    public Result<SendTransactionRequestBody> sendTransaction(SendTransactionRequestBody request) {
            Double amount = request.getAmount();
            Double fee = request.getFee();

            if(request.getClientId() != null) {
                ResourceEntity senderResource = new ResourceEntity(
                        block.getId(), t.getOwner(), Resource.Type.VALUE.name(), amount.toString(), true, t.getTimestamp(), t.getRequestSignature()
                );
                resourceRepository.save(senderResource);
            }

            if(t.getRecipient() != null) {
                ResourceEntity recipientResource = new ResourceEntity(
                        block.getId(), t.getRecipient(), Resource.Type.VALUE.name(), amount.toString(), false, t.getTimestamp(), t.getRequestSignature()
                );
                resourceRepository.save(recipientResource);
            }

            if (fee > 0) {
                ResourceEntity feeResource = new ResourceEntity(
                        block.getId(), proposerId, Resource.Type.VALUE.name(), fee.toString(), false, t.getTimestamp(), t.getRequestSignature()
                );
                resourceRepository.save(feeResource);
            }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends Request, T extends Serializable> Response<T> invokeUnordered(R request, ConsensusRequest.Type t0) {
        return invoke(request, t0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends Request, T extends Serializable> Response<T> invokeOrdered(R request, ConsensusRequest.Type t0) {
        return invoke(request, t0);
    }

    private <R extends Request, T extends Serializable> Response<T> invoke(R request, ConsensusRequest.Type t0) {
        try {
            ConsensusRequest consensusRequest = new ConsensusRequest(request, t0, 0);

            CountDownLatch latch = new CountDownLatch(1);
            BlockmessReplyListener listener = new BlockmessReplyListener(latch, quorum);
            super.invokeAsyncOperation(dataToBytes(consensusRequest), listener);
            latch.await(TIMEOUT_PERIOD, TimeUnit.MILLISECONDS);

            ConsensusResponse consensusResponse = listener.getResponse();
            if(consensusResponse != null) {
                Result<T> result = consensusResponse.extractResult();
                Response<T> response = null;
                if(result.valid())
                    response = new Response<>(result.value());
                else
                    response = new Response<>(result);
                response.replicaResponses(Collections.emptyList());
                return response;
            }
            else {
                return new Response<>(Status.NOT_AVAILABLE, "Not enough correct replicas");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public OffsetDateTime getLastResourceDate(byte[] owner) {
        return Optional.ofNullable(resourceRepository.findFirstByOwnerOrderByTimestampAsc(bytesToString(owner))).map(ResourceEntity::getTimestamp).orElse(OffsetDateTime.MIN);
    }
}
