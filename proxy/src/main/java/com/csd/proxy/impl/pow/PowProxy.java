package com.csd.proxy.impl.pow;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import com.csd.common.request.Request;
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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.*;

@Component
public class PowProxy extends AsynchServiceProxy {

    private static final int TIMEOUT_PERIOD = 5000;
    public final ResourceRepository resourceRepository;

    public PowProxy(Environment environment, ResourceRepository resourceRepository) {
        super(environment.getProperty("proxy.id", Integer.class));
        //super(environment.getProperty("proxy.id", Integer.class), (String)null, new DefaultKeyLoader());
        this.resourceRepository = resourceRepository;
    }

    @SuppressWarnings("unchecked")
    public <R extends Request, T extends Serializable> Response<T> invokeUnordered(R request, ConsensusRequest.Type t0) {
        return invoke(request, t0, TOMMessageType.UNORDERED_REQUEST);
    }

    @SuppressWarnings("unchecked")
    public <R extends Request, T extends Serializable> Response<T> invokeOrdered(R request, ConsensusRequest.Type t0) {
        return invoke(request, t0, TOMMessageType.ORDERED_REQUEST);
    }

    private <R extends Request, T extends Serializable> Response<T> invoke(R request, ConsensusRequest.Type t0, TOMMessageType t1) {
        try {
            long lastEntry =  Optional.ofNullable(resourceRepository.findTopByOrderByIdDesc()).map(ResourceEntity::getId).orElse(0L);
            ConsensusRequest consensusRequest = new ConsensusRequest(request, t0, lastEntry);

            CountDownLatch latch = new CountDownLatch(1);
            PowReplyListener listener = new PowReplyListener(this, latch);
            super.invokeAsynchRequest(dataToBytes(consensusRequest), listener, t1);
            latch.await(TIMEOUT_PERIOD, TimeUnit.MILLISECONDS);

            ConsensusResponse consensusResponse = listener.getResponse();
            if(consensusResponse != null) {
                resourceRepository.saveAll(Arrays.stream(consensusResponse.getMissingEntries()).map(ResourceEntity::new).collect(Collectors.toList()));
                Result<T> result = consensusResponse.extractResult();
                Response<T> response = null;
                if(result.valid())
                    response = new Response<>(result.value());
                else
                    response = new Response<>(result);
                response.replicaResponses(listener.getReplicaResponses());
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

    public OffsetDateTime getLastTrxDate(byte[] owner) {
        return Optional.ofNullable(resourceRepository.findFirstByOwnerOrderByTimestampAsc(bytesToString(owner))).map(ResourceEntity::getTimestamp).orElse(OffsetDateTime.MIN);
    }
}

