package com.csd.replica.consensuslayer.pow;

import com.csd.common.cryptography.suites.digest.IDigestSuite;
import com.csd.common.datastructs.MerkleTree;
import com.csd.common.item.Wallet;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static com.csd.common.util.Serialization.bytesToHex;

public class MinerThread extends Thread {

    public static final int MAX_PROOF_LENGTH = 255;

    private final int processId;

    private final Wallet wallet;

    private ReplicaBroadcast replicaBroadcast;
    private final ReplicaService replicaService;
    private final IDigestSuite transactionDigestSuite;
    private final IDigestSuite blockDigestSuite;
    private final int blockSize;
    private final int difficultyTarget;
    private final Double blockReward;

    private boolean restart = false;

    public MinerThread(int processId, ReplicaService replicaService, IDigestSuite transactionDigestSuite, IDigestSuite blockDigestSuite, int blockSize, int difficultyTarget, Double blockReward) {
        this.processId = processId;
        try {
            this.wallet = new Wallet(processId + "@miner", UUID.randomUUID().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.replicaService = replicaService;
        this.transactionDigestSuite = transactionDigestSuite;
        this.blockDigestSuite = blockDigestSuite;
        this.blockSize = blockSize;
        this.difficultyTarget = difficultyTarget;
        this.blockReward = blockReward;
    }

    public void run(){
        while (true) {
            List<Transaction> transactions = replicaService.getTransactionBatch(blockSize-1);
            if (transactions.size() == blockSize-1) {
                Transaction coinbase = new Transaction(
                        UUID.randomUUID().toString(),
                        null,
                        wallet.clientId,
                        blockReward,
                        null,
                        OffsetDateTime.now(),
                        null
                );
                transactions.add(coinbase);

                Block block = mine(transactions);
                if(block != null) {
                    BlockProposal blockProposal = new BlockProposal(wallet.clientId, wallet.signatureSuite, block, coinbase);
                    if (replicaBroadcast == null) {
                        initBroadcastService();
                    }
                    replicaBroadcast.broadcast(blockProposal);
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void initBroadcastService() {
        try {
            replicaBroadcast = new ReplicaBroadcast(processId);
            Thread.sleep(10000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Block mine(List<Transaction> transactions) {
        BlockHeaderEntity previousBlock = replicaService.getLastBlock();
        byte[] previousBlockHash = null;
        if(previousBlock != null) {
            previousBlockHash = previousBlock.getHash();
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
                byte[] blockHash = blockDigestSuite.digest(block.serializedBlock());
                hex = bytesToHex(blockHash);
                length = Math.min(MAX_PROOF_LENGTH, length*1.001);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }

            if(restart) {
                restart = false;
                return null;
            }
        }
        while(!hex.startsWith(challenge));

        return block;
    }

    public void restart() {
        this.restart = true;
    }

    public IDigestSuite getTransactionDigestSuite() {
        return transactionDigestSuite;
    }

    public int getDifficultyTarget() {
        return difficultyTarget;
    }

    public IDigestSuite getBlockDigestSuite() {
        return blockDigestSuite;
    }

    public Double getBlockReward() {
        return blockReward;
    }
}
