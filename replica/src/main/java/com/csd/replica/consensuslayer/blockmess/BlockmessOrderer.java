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

public class BlockmessOrderer {

}
