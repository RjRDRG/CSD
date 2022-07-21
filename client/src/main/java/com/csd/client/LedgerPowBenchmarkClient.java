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
    private static final int MAX_CLIENTS = 8;
    private static final int NUM_BLOCKS = 5;
    private static final String ORIGIN = UUID.randomUUID().toString();

    public static void main(String[] args) {
        NullConsole console = new NullConsole();
        int nClients = MIN_CLIENTS;

        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        LedgerClient.createWallet(ORIGIN, UUID.randomUUID().toString(), console);

        long initialBlock = -1;

        try {
            double balance = 0;
            while (balance == 0) {
                LedgerClient.loadMoney(ORIGIN, Double.MAX_VALUE/100, console);
                balance = LedgerClient.getBalance(ORIGIN, console);
            }
        } catch (Exception e) {
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

    static final int block_size = 10;

    static void createGraph(Map<Integer,List<Long>> result) throws IOException {
        double[] xData = result.keySet().stream().map(i -> (double)i).mapToDouble(d->d).toArray();

        double[] median = result.values().stream()
                .map(c -> median().compute(c))
                .mapToDouble(d->d).toArray();

        double[] p75 = result.values().stream()
                .map(c -> percentiles().index(75).compute(c))
                .mapToDouble(d->d).toArray();

        double[] p25 = result.values().stream()
                .map(c -> percentiles().index(25).compute(c))
                .mapToDouble(d->d).toArray();


        XYChart chart = new XYChartBuilder().title("Latency Pow").xAxisTitle("Clients").yAxisTitle("ms").build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

        chart.addSeries("median", xData, median);
        chart.addSeries("p75", xData, p75);
        chart.addSeries("p25", xData, p25);

        new SwingWrapper(chart).displayChart();

        BitmapEncoder.saveBitmap(chart, "./Latency_Chart_PoW", BitmapEncoder.BitmapFormat.PNG);

        XYChart chart1 = new XYChartBuilder().title("Throughput Pow").xAxisTitle("Clients").yAxisTitle("ms").build();

        chart1.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

        chart1.addSeries("median", xData,  Arrays.stream(median).map(d -> block_size/d).toArray());
        chart1.addSeries("p75", xData,  Arrays.stream(p75).map(d -> block_size/d).toArray());
        chart1.addSeries("p25", xData,  Arrays.stream(p25).map(d -> block_size/d).toArray());

        new SwingWrapper(chart1).displayChart();

        BitmapEncoder.saveBitmap(chart1, "./Throughput_Chart_PoW", BitmapEncoder.BitmapFormat.PNG);

        //BitmapEncoder.saveBitmapWithDPI(chart, "./Sample_Chart_300_DPI", BitmapEncoder.BitmapFormat.PNG, 300);
    }
}
