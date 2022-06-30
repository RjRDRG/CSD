package com.csd.client;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class LedgerBenchmarkClient {

    private static final int MIN_CLIENTS = 10;
    private static final int MAX_CLIENTS = 20;
    private static final int AMOUNT = 10;

    private static final int ROUTINES = 100;
    private static final int WARMUP = 10;
    private static final int COOLDOWN = 10;
    private static final String DEST = UUID.randomUUID().toString();

    public static void main(String[] args) throws Exception {
        NullConsole console = new NullConsole();
        int nClients = MIN_CLIENTS;

        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        LedgerClient.createWallet(DEST, UUID.randomUUID().toString(), console);

        while(nClients <= MAX_CLIENTS){
            CountDownLatch latch = new CountDownLatch(nClients);

            long[] latencies = new long[nClients];
            long startWorkload = System.currentTimeMillis();

            for(int i = 0; i < nClients; i++) {
                final int client_number = i;

                new Thread(() -> {
                    long start, end;

                    String id = UUID.randomUUID().toString();
                    LedgerClient.createWallet(id, UUID.randomUUID().toString(), console);

                    LedgerClient.loadMoney(id, AMOUNT*ROUTINES*2, console);

                    long latencyAccum = 0;

                    for (int j=0; j<ROUTINES; j++) {
                        start = System.currentTimeMillis();
                        LedgerClient.sendTransaction(id, DEST, AMOUNT, 0, console);
                        if(j <= ROUTINES-COOLDOWN && j >= WARMUP) {
                            end = System.currentTimeMillis();
                            long latency = end - start;
                            latencyAccum += latency;
                        }
                    }

                    latencies[client_number] = latencyAccum/(ROUTINES-WARMUP-COOLDOWN);

                    latch.countDown();
                }).start();
            }

            latch.await();

            long endWorkload = System.currentTimeMillis();

            long averageLatency = Arrays.stream(latencies).sum()/nClients;
            long averageThroughput = nClients*ROUTINES/((endWorkload-startWorkload)/1000);

            System.out.println("[ Clients = " + nClients + " ]");
            System.out.println("Latency: " + averageLatency + " millis");
            System.out.println("Throughput: " + averageThroughput + " requests/second");
            System.out.println();

            nClients *= 2;
        }
    }
}
