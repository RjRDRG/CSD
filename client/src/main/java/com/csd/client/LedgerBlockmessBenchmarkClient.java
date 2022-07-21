package com.csd.client;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.slf4j.LoggerFactory;

import static com.google.common.math.Quantiles.median;
import static com.google.common.math.Quantiles.percentiles;

public class LedgerBlockmessBenchmarkClient {

    private static final int MIN_CLIENTS = 1;
    private static final int MAX_CLIENTS = 8;
    private static final double AMOUNT = 1;
    private static final int NUM_BLOCKS = 5;
    private static final String ORIGIN = UUID.randomUUID().toString();

    public static void main1(String[] args) throws Exception {
        NullConsole console = new NullConsole();
        int nClients = MIN_CLIENTS;

        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        LedgerClient.createWallet(ORIGIN, UUID.randomUUID().toString(), console);

        LedgerClient.loadMoney(ORIGIN, Double.MAX_VALUE, console);

        Map<Integer,List<Long>> rLatencies = new HashMap<>();
        Map<Integer,List<Double>> rThourghput = new HashMap<>();

        while(nClients <= MAX_CLIENTS){
            CountDownLatch latch = new CountDownLatch((int) nClients);

            Map<Integer, Long> latencies = new ConcurrentHashMap<>();

            System.out.println("Starting round: " + nClients);

            for(int i = 0; i < nClients; i++) {
                final int client_number = i;

                new Thread(() -> {
                    long start, end;

                    String id = UUID.randomUUID().toString();
                    LedgerClient.createWallet(id, UUID.randomUUID().toString(), console);

                    for (int j=0; j<NUM_BLOCKS; j++) {
                        start = System.currentTimeMillis();
                        LedgerClient.sendTransactionOnce(ORIGIN, id, AMOUNT, 0, console);
                        end = System.currentTimeMillis();
                        long latency = end - start;
                        latencies.putIfAbsent(j, latency);
                    }

                    latch.countDown();
                }).start();
            }

            latch.await();

            int finalNClients = nClients;
            List<Double> throughputs = latencies.values().stream().map(l -> ((double)finalNClients/l)*1000).collect(Collectors.toList());

            System.out.println("[ Clients = " + nClients + " ]");
            System.out.println("Latency: " + median().compute(latencies.values())  + " millis");
            System.out.println("Throughput: " + median().compute(throughputs) + " requests/second");
            System.out.println();

            rLatencies.put(nClients, new ArrayList<>(latencies.values()));
            rThourghput.put(nClients, throughputs);

            nClients *= 2;
        }

        createGraph(rLatencies, rThourghput);
    }

    static void createGraph(Map<Integer, List<Long>> rLatencies, Map<Integer, List<Double>> rThourghput) throws IOException {
        double[] xData = rLatencies.keySet().stream().map(i -> (double)i).mapToDouble(d->d).toArray();

        double[] median = rLatencies.values().stream()
                .map(c -> median().compute(c))
                .mapToDouble(d->d).toArray();

        double[] p75 = rLatencies.values().stream()
                .map(c -> percentiles().index(75).compute(c))
                .mapToDouble(d->d).toArray();

        double[] p25 = rLatencies.values().stream()
                .map(c -> percentiles().index(25).compute(c))
                .mapToDouble(d->d).toArray();


        XYChart chart = new XYChartBuilder().title("Latency Blockmess").xAxisTitle("Clients").yAxisTitle("ms").build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

        chart.addSeries("median", xData, median);
        chart.addSeries("p75", xData, p75);
        chart.addSeries("p25", xData, p25);

        new SwingWrapper(chart).displayChart();

        BitmapEncoder.saveBitmap(chart, "./Latency_Chart_Blockmess", BitmapEncoder.BitmapFormat.PNG);

        median = rThourghput.values().stream()
                .map(c -> median().compute(c))
                .mapToDouble(d->d).toArray();

        p75 = rThourghput.values().stream()
                .map(c -> percentiles().index(75).compute(c))
                .mapToDouble(d->d).toArray();

        p25 = rThourghput.values().stream()
                .map(c -> percentiles().index(25).compute(c))
                .mapToDouble(d->d).toArray();

        chart = new XYChartBuilder().title("Throughput Blockmess").xAxisTitle("Clients").yAxisTitle("requests/s").build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

        chart.addSeries("median", xData, median);
        chart.addSeries("p75", xData, p75);
        chart.addSeries("p25", xData, p25);

        new SwingWrapper(chart).displayChart();

        BitmapEncoder.saveBitmap(chart, "./Throughput_Chart_Blockmess", BitmapEncoder.BitmapFormat.PNG);
    }
}
