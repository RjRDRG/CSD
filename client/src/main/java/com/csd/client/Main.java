package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.formdev.flatlaf.FlatDarculaLaf;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Main {

    public static void main(String[] args) throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        LedgerClient.createWallet("default@csd.com", UUID.randomUUID().toString(), null);

        FlatDarculaLaf.setup();
        new LedgerSwingClient();
    }

}
