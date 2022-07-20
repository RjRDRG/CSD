package com.csd.client;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class LedgerBenchmarkClient {

    private static final double MIN_CLIENTS = 50;
    private static final double MAX_CLIENTS = 50;
    private static final double AMOUNT = 10;

    private static final double ROUTINES = 1;
    private static final double WARMUP = 0;
    private static final double COOLDOWN = 0;
    private static final String DEST = UUID.randomUUID().toString();

    public static void main8(String[] args) throws Exception {
        NullConsole console = new NullConsole();
        double nClients = MIN_CLIENTS;

        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        LedgerClient.createWallet(DEST, UUID.randomUUID().toString(), console);

        while(nClients <= MAX_CLIENTS){
            CountDownLatch latch = new CountDownLatch((int) nClients);

            double[] latencies = new double[(int) nClients];
            double startWorkload = System.currentTimeMillis();

            System.out.println("Starting round: " + nClients);

            for(int i = 0; i < nClients; i++) {
                final int client_number = i;

                new Thread(() -> {
                    double start, end;

                    String id = UUID.randomUUID().toString();
                    LedgerClient.createWallet(id, UUID.randomUUID().toString(), console);

                    LedgerClient.loadMoney(id, AMOUNT*ROUTINES*2, console);

                    double latencyAccum = 0;

                    for (int j=0; j<ROUTINES; j++) {
                        start = System.currentTimeMillis();
                        LedgerClient.sendTransactionOnce(id, DEST, AMOUNT, 0, console);
                        if(j <= ROUTINES-COOLDOWN && j >= WARMUP) {
                            end = System.currentTimeMillis();
                            double latency = end - start;
                            latencyAccum += latency;
                        }
                    }

                    latencies[client_number] = latencyAccum/(ROUTINES-WARMUP-COOLDOWN);

                    latch.countDown();
                }).start();
            }

            latch.await();

            double endWorkload = System.currentTimeMillis();

            double averageLatency = Arrays.stream(latencies).sum()/nClients;
            double averageThroughput = ((nClients * ROUTINES) / (endWorkload - startWorkload)) * 1000;

            System.out.println(endWorkload-startWorkload);
            System.out.println(nClients*ROUTINES);

            System.out.println("[ Clients = " + nClients + " ]");
            System.out.println("Latency: " + averageLatency + " millis");
            System.out.println("Throughput: " + averageThroughput + " requests/second");
            System.out.println();

            nClients *= 2;
        }
    }
}
