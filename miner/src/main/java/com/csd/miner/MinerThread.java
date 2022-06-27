package com.csd.miner;

import bftsmart.tom.ReplicaContext;
import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.datastructs.MerkleTree;
import com.csd.common.util.Serialization;
import com.csd.replica.datalayer.Block;
import com.csd.replica.datalayer.BlockHeaderEntity;
import com.csd.replica.datalayer.Transaction;
import com.csd.replica.servicelayer.ReplicaService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.bytesToHex;
import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class MinerThread extends Thread {

    public static final int MAX_PROOF_LENGTH = 255;
    private final ReplicaContext replicaContext;
    private final ReplicaService replicaService;

    private final IDigestSuite transactionDigestSuite;
    private final IDigestSuite blockDigestSuite;
    private final int blockSize;
    private final int difficultyTarget;

    public MinerThread(ReplicaContext replicaContext, ReplicaService replicaService, IDigestSuite transactionDigestSuite, IDigestSuite blockDigestSuite, int blockSize, int difficultyTarget) {
        this.replicaContext = replicaContext;
        this.replicaService = replicaService;
        this.transactionDigestSuite = transactionDigestSuite;
        this.blockDigestSuite = blockDigestSuite;
        this.blockSize = blockSize;
        this.difficultyTarget = difficultyTarget;
    }

    public void run(){
        while (true) {
            List<Transaction> transactions = replicaService.getTransactionBatch(blockSize);
            if (transactions.size() == blockSize) {
                mine(transactions);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void mine(List<Transaction> transactions) {
        BlockHeaderEntity previousBlock = replicaService.getLastBlock();
        byte[] previousBlockHash = null;
        if(previousBlock != null) {
            previousBlockHash = previousBlock.getDigest(blockDigestSuite);
        }

        Block block = new Block(
                previousBlockHash,
                new MerkleTree(transactions.stream().map(Serialization::dataToBytesDeterministic).collect(Collectors.toList()), transactionDigestSuite).getRoot().sig,
                difficultyTarget,
                transactions.stream().map(Transaction::getId).collect(Collectors.toList()),
                OffsetDateTime.now()
        );

        String challenge = StringUtils.repeat('0', difficultyTarget);
        String hex;
        double length = 4;
        do {
            try {
                byte[] proof = RandomStringUtils.random((int) length, true, true).getBytes(StandardCharsets.UTF_8);
                block.setProof(proof);
                byte[] blockHash = blockDigestSuite.digest(dataToBytesDeterministic(block));
                hex = bytesToHex(blockHash);
                length = Math.min(MAX_PROOF_LENGTH, length*1.001);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        while(!hex.startsWith(challenge));

        int[] targets =
        replicaContext.getServerCommunicationSystem().getServersConn().send();
    }
}
