package com.csd.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class LedgerBenchmarkClient {
    private static final int MAX_CLIENTS = 32;
    private static final int MONEY = 10;
    private static final String DEST = "0";

    public static void main(String[] args) throws Exception {
        AtomicReference<Long> avgLatency = new AtomicReference<>(0L);
        NullConsole console = new NullConsole();
        int nClients = 1;

        LedgerClient.createWallet(DEST, UUID.randomUUID().toString(), console);

        while(nClients <= MAX_CLIENTS){
            CountDownLatch latch = new CountDownLatch(nClients);
            long sT = System.currentTimeMillis();

            for(int i = 0; i < nClients; i++) {
                new Thread(() -> {
                    long start = System.currentTimeMillis();

                    String id = UUID.randomUUID().toString();
                    LedgerClient.createWallet(id, UUID.randomUUID().toString(), console);

                    LedgerClient.loadMoney(id, MONEY, console);

                    LedgerClient.sendTransaction(id, DEST, MONEY, 0, console);

                    avgLatency.updateAndGet(v -> v + (System.currentTimeMillis() - start));

                    latch.countDown();
                });
            }

            latch.await();

            System.out.println("[ Clients = " + nClients + " ]");
            System.out.println("Latency: " + avgLatency.get()/nClients + " (sum-latencies/n-clients");
            System.out.println("Throughput: " + nClients/(System.currentTimeMillis() - sT) + " (n-clients/total-elapsed-time");
            System.out.println();

            nClients *= 2;
        }
    }
}
