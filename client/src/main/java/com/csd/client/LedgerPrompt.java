package com.csd.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LedgerPrompt {

    static String manualToString(){
        return "Available operations : \n" +
                "h - Help;                                             Eg: h \n"+
                "w - List wallets ids;                                 Eg: w \n"+
                "O - Set the proxy port;                               Eg: 0 {8080, 8081, 8082, 8083} \n" +
                "1 - Create wallet;                                    Eg: 1 {wallet_id} \n" +
                "a - Obtain tokens;                                    Eg: a {wallet_id} {amount}\n" +
                "c - Consult balance of a certain client;              Eg: c {wallet_id}\n" +
                "z - Exit                                              Eg: z";
    }

    public LedgerPrompt() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.println(manualToString());

        while(true) {

            try {
                String[] command = in.readLine().split(" ");
                char op = command[0].charAt(0);
                switch (op) {
                    case 'h':
                        System.out.println(manualToString());
                        break;
                    case 'w':
                        System.out.println(LedgerClient.wallets.keySet());
                        break;
                    case '0':
                        LedgerClient.proxyPort = command[1];
                        break;
                    case '1':
                        LedgerClient.wallets.put(command[1], new WalletDetails());
                        break;
                    case 'a':
                        LedgerClient.loadMoney(command[1], Integer.parseInt(command[2]));
                        break;
                    case 'c':
                        LedgerClient.getBalance(command[1]);
                        break;
                    case 'z':
                        return;
                    default:
                        System.out.println("Chosen operation does not exist. Please try again.");
                        break;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
