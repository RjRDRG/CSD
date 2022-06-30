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
import com.csd.proxy.ledger.ResourceEntity;
import com.csd.proxy.ledger.ResourceRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.csd.common.util.Serialization.*;

@Component
public class BlockmessProxyOrderer extends ApplicationInterface {

    private static final int TIMEOUT_PERIOD = 20;
    private final int quorum;
    private final RequestValidator validator;

    public final ResourceRepository resourceRepository;

    public BlockmessProxyOrderer(Environment environment, ResourceRepository resourceRepository) throws Exception {
        super(new String[]{});
        this.quorum = environment.getProperty("proxy.quorum.size" , int.class);
        this.validator = new RequestValidator(quorum, environment.getProperty("proxy.number" , int.class));
        this.resourceRepository = resourceRepository;
    }

    public  <R extends Request, T extends Serializable> Response<T> invoke(R request, ConsensusRequest.Type t0) {
        try {
            ConsensusRequest consensusRequest = new ConsensusRequest(request, t0, 0);
            System.out.println("invokeSt");
            CountDownLatch latch = new CountDownLatch(1);
            BlockmessReplyListener listener = new BlockmessReplyListener(latch, quorum);
            super.invokeAsyncOperation(dataToBytes(consensusRequest), listener);
            latch.await(TIMEOUT_PERIOD, TimeUnit.MINUTES);
            System.out.println("invokeEnd");
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
                System.out.println("Not available");
                return new Response<>(Status.NOT_AVAILABLE, "Not enough correct replicas");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public byte[] processOperation(byte[] command) {
        try {
            System.out.println("Executing");
            return dataToBytes(execute(bytesToData(command)));
        } catch (Exception e) {
            e.printStackTrace();
            Result<Serializable> result = Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
            return dataToBytes(new ConsensusResponse(result.encode(), new Resource[0]));
        }
    }

    public ConsensusResponse execute(ConsensusRequest consensusRequest) {
        switch (consensusRequest.getType()) {
            case TRANSFER: {
                SendTransactionRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, getLastResourceDate(request.getClientId().get(0)), true);
                Result<SendTransactionRequestBody> result =  v.valid() ? sendTransaction(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), null);
            }
            case LOAD: {
                LoadMoneyRequestBody request = consensusRequest.extractRequest();
                var v = validator.validate(request, getLastResourceDate(request.getClientId().get(0)), true);
                Result<LoadMoneyRequestBody> result =  v.valid() ? loadMoney(request) : Result.error(v);
                return new ConsensusResponse(result.encode(), null);
            }
            default: {
                Result<Serializable> result = Result.error(Status.NOT_IMPLEMENTED, consensusRequest.getType().name());
                return new ConsensusResponse(result.encode(), null);
            }
        }
    }

    public Result<SendTransactionRequestBody> sendTransaction(SendTransactionRequestBody request) {
        try{
            double amount = request.getAmount();

            if(request.getClientId() != null) {
                ResourceEntity senderResource = new ResourceEntity(
                        -1, request.getClientId().get(0), Resource.Type.VALUE.name(), Double.toString(amount), true, request.getNonce(), request.getClientSignature().get(0).getSignature()
                );
                resourceRepository.save(senderResource);
            }

            if(request.getRecipient() != null) {
                ResourceEntity recipientResource = new ResourceEntity(
                        -1, request.getRecipient(), Resource.Type.VALUE.name(), Double.toString(amount), false, request.getNonce(), request.getClientSignature().get(0).getSignature()
                );
                resourceRepository.save(recipientResource);
            }

            return Result.ok(request);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public Result<LoadMoneyRequestBody> loadMoney(LoadMoneyRequestBody request) {
        try {
            double amount = request.getAmount();

            if(request.getClientId() != null) {
                ResourceEntity senderResource = new ResourceEntity(
                        -1, request.getClientId().get(0), Resource.Type.VALUE.name(), Double.toString(amount), false, request.getNonce(), request.getClientSignature().get(0).getSignature()
                );
                resourceRepository.save(senderResource);
            }

            return Result.ok(request);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Status.INTERNAL_ERROR, Arrays.toString(e.getStackTrace()));
        }
    }

    public OffsetDateTime getLastResourceDate(byte[] owner) {
        return Optional.ofNullable(resourceRepository.findFirstByOwnerOrderByTimestampAsc(bytesToString(owner))).map(ResourceEntity::getTimestamp).orElse(OffsetDateTime.MIN);
    }
}
