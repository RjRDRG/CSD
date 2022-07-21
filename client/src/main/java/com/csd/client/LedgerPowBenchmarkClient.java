package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.google.common.math.Quantiles.median;
import static com.google.common.math.Quantiles.percentiles;

public class LedgerPowBenchmarkClient {

    private static final int MIN_CLIENTS = 1;
    private static final int MAX_CLIENTS = 4;
    private static final int NUM_BLOCKS = 3;
    private static final String ORIGIN = UUID.randomUUID().toString();

    public static void main(String[] args) {
        NullConsole console = new NullConsole();
        int nClients = MIN_CLIENTS;

        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        LedgerClient.createWallet(ORIGIN, UUID.randomUUID().toString(), console);

        long initialBlock = -1;

        try {
            for (int i=0; i<6; i++){
                LedgerClient.loadMoney(ORIGIN, Double.MAX_VALUE/10, console);
            }
            Thread.sleep(20000);
            double balance = 0;
            while (balance == 0) {
                balance = LedgerClient.getBalance(ORIGIN, console);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Map<Integer,List<Long>> result = new HashMap<>();

        List<Thread> workerThreads = new LinkedList<>();

        while(nClients <= MAX_CLIENTS){
            System.out.println("Starting round: " + nClients);

            List<Long> latencies = new LinkedList<>();

            for(int i = workerThreads.size(); i < nClients; i++) {
                Thread thread = new Thread(() -> {
                    String id = UUID.randomUUID().toString();
                    LedgerClient.createWallet(id, UUID.randomUUID().toString(), console);

                    while (true) {
                        LedgerClient.sendTransactionOnce(ORIGIN, id, 1, 0, console);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
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

            initialBlock = latencies.size()-1;

            double averageLatency = ((double)latencies.stream().reduce(0L, Long::sum))/latencies.size();
            int blockSize = 5;
            double averageThroughput = (blockSize/averageLatency) * 1000;

            result.put(nClients, latencies);

            System.out.println("[ Clients = " + nClients + " ]");
            System.out.println("Latency: " + averageLatency + " millis");
            System.out.println("Throughput: " + averageThroughput + " requests/second");
            System.out.println();

            nClients *= 2;
        }

        workerThreads.forEach(Thread::interrupt);

        try {
            createGraph(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void createGraph(Map<Integer,List<Long>> result) throws IOException {
        double[] xData = result.keySet().stream().map(i -> (double)i).mapToDouble(d->d).toArray();
        double[] median = result.values().stream()
                .map(c -> median().compute(c))
                .mapToDouble(d->d).toArray();

        double[] per75 = result.values().stream()
                .map(c -> percentiles().index(75).compute(c))
                .mapToDouble(d->d).toArray();

        double[] per25 = result.values().stream()
                .map(c -> percentiles().index(25).compute(c))
                .mapToDouble(d->d).toArray();


        XYChart chart = new XYChartBuilder().title("Latency Pow").xAxisTitle("Clients").yAxisTitle("ms").build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

        chart.addSeries("median", xData, median);
        chart.addSeries("per75", xData, per75);
        chart.addSeries("per25", xData, per25);

        new SwingWrapper(chart).displayChart();

        BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapEncoder.BitmapFormat.PNG);

        //BitmapEncoder.saveBitmapWithDPI(chart, "./Sample_Chart_300_DPI", BitmapEncoder.BitmapFormat.PNG, 300);
    }
}
