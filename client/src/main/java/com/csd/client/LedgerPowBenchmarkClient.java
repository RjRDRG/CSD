package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class LedgerPowBenchmarkClient {

    private static final double MIN_CLIENTS = 5;
    private static final double MAX_CLIENTS = 20;
    private static final int NUM_BLOCKS = 5;
    private static final String ORIGIN = UUID.randomUUID().toString();

    public static void main(String[] args) {
        NullConsole console = new NullConsole();
        double nClients = MIN_CLIENTS;

        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        LedgerClient.createWallet(ORIGIN, UUID.randomUUID().toString(), console);

        long initialBlock = -1;

        while(nClients <= MAX_CLIENTS){
            System.out.println("Starting round: " + nClients);

            List<Long> latencies = new LinkedList<>();

            for (int i=0; i<6; i++){
                LedgerClient.loadMoney(ORIGIN, Double.MAX_VALUE/10, console);
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            List<Thread> workerThreads = new LinkedList<>();

            for(int i = 0; i < nClients; i++) {
                Thread thread = new Thread(() -> {
                    String id = UUID.randomUUID().toString();
                    LedgerClient.createWallet(id, UUID.randomUUID().toString(), console);

                    while (true) {
                        LedgerClient.sendTransactionOnce(ORIGIN, id, 1, 0, console);
                    }
                });
                thread.start();
                workerThreads.add(thread);
            }

            long block = initialBlock;
            long startTime = System.currentTimeMillis();;
            while (latencies.size() < NUM_BLOCKS) {
                long current = LedgerClient.getBlock();
                if(current>block) {
                    long endTime = System.currentTimeMillis();
                    latencies.add(endTime - startTime);
                    startTime = endTime;
                    block = current;
                    System.out.println("New Block: " + block + ", Latency: " + latencies.get(latencies.size()-1));
                }
            }

            workerThreads.forEach(Thread::interrupt);

            initialBlock = latencies.size()-1;

            double averageLatency = ((double)latencies.stream().reduce(0L, Long::sum))/latencies.size();
            int blockSize = 5;
            double averageThroughput = (blockSize/averageLatency) * 1000;

            System.out.println("[ Clients = " + nClients + " ]");
            System.out.println("Latency: " + averageLatency + " millis");
            System.out.println("Throughput: " + averageThroughput + " requests/second");
            System.out.println();

            nClients *= 2;
        }
    }
}
