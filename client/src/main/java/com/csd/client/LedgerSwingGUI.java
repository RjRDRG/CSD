package com.csd.client;

import com.csd.client.ui.JGridBagPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LedgerSwingGUI extends JFrame{

    public LedgerSwingGUI() {
        setTitle("Wallet");

        getContentPane().setLayout(new BorderLayout());

        JGridBagPanel top = new JGridBagPanel();
        top.load(0,0, new JSeparator(SwingConstants.HORIZONTAL)).add();
        top.load(0,1, Box.createRigidArea(new Dimension(0,20))).add();
        getContentPane().add(top, BorderLayout.PAGE_START);
        getContentPane().add(Box.createRigidArea(new Dimension(0,20)), BorderLayout.PAGE_END);
        getContentPane().add(Box.createRigidArea(new Dimension(20,0)), BorderLayout.LINE_START);
        getContentPane().add(Box.createRigidArea(new Dimension(20,0)), BorderLayout.LINE_END);
        getContentPane().add(new MainPanel(), BorderLayout.CENTER);

        setSize(new Dimension(1000, 1000));
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    static class MainPanel extends JPanel {
        JLabel walletLabel = new JLabel("Wallet");
        JComboBox<String> wallets= new JComboBox<>();
        JTextField newWalletName = new JTextField();
        JButton createWalletButton = new JButton("New Wallet");

        JLabel loadMoneyLabel = new JLabel("Load Money");
        JTextField loadMoneyAmount = new JTextField();
        JButton loadMoneyExec = new JButton("Exec");

        JLabel getBalanceLabel = new JLabel("Get Balance");
        JButton getBalanceExec = new JButton("Exec");

        JLabel sendTransactionLabel = new JLabel("Send Transaction");
        JComboBox<String> destination= new JComboBox<>();
        JTextField sendTransactionAmount = new JTextField();
        JButton sendTransactionExec = new JButton("Exec");

        JLabel getGlobalValueLabel = new JLabel("Get Global Value");
        JButton getGlobalValueExec = new JButton("Exec");

        JLabel getExtractLabel = new JLabel("Get Extract");
        JButton getExtractExec = new JButton("Exec");

        JLabel getLedgerLabel = new JLabel("Get Ledger");
        JButton getLedgerExec = new JButton("Exec");

        JLabel getTotalValueLabel = new JLabel("Get Total Value");
        JButton getTotalValueExec = new JButton("Exec");

        JTextArea result = new JTextArea();

        public MainPanel() {
            setLayout(new BorderLayout());
            JGridBagPanel gp0 = new JGridBagPanel();

            gp0.load(0,0,walletLabel).removeScaleY().removeScaleX().setAnchorLeft().add();
            gp0.load(1,0,wallets).removeScaleY().removeScaleX().setAnchorLeft().add();
            gp0.load(2,0,newWalletName).removeScaleY().setLeftPad(200).setAnchorRight().add();
            gp0.load(3,0,createWalletButton).removeScaleY().removeScaleX().setAnchorRight().add();

            createWalletButton.addActionListener(e -> {
                try {
                    LedgerClient.wallets.put(newWalletName.getName(), new WalletDetails());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            JGridBagPanel gp1 = new JGridBagPanel();

            //components of op
            gp1.load(0,0,loadMoneyLabel).removeScaleY().removeScaleX().add();
            gp1.load(1,0,loadMoneyAmount).removeScaleY().removeScaleX().add();
            gp1.load(2,0,loadMoneyExec).removeScaleY().removeScaleX().add();
            loadMoneyExec.addActionListener(e -> {
                try {
                    result.setText(LedgerClient.loadMoney(
                            (String) wallets.getSelectedItem(),
                            Double.parseDouble(loadMoneyAmount.getText())).toString()
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            gp1.load(0,1,getBalanceLabel).removeScaleY().removeScaleX().add();
            gp1.load(1,1,getBalanceExec).setWidth(2).removeScaleY().removeScaleX().add();
            getBalanceExec.addActionListener(e -> {
                try {
                    result.setText(LedgerClient.getBalance(
                            (String) wallets.getSelectedItem()
                    ).toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            gp1.load(0,2,sendTransactionLabel).removeScaleY().removeScaleX().add();
            gp1.load(1,2,destination).removeScaleY().removeScaleX().add();
            gp1.load(2,2,sendTransactionAmount).removeScaleY().removeScaleX().add();
            gp1.load(3,2,sendTransactionExec).removeScaleY().removeScaleX().add();
            sendTransactionExec.addActionListener(e -> {
                try {
                    result.setText(LedgerClient.sendTransaction(
                            (String) wallets.getSelectedItem(),
                            (String) destination.getSelectedItem(),
                            Double.parseDouble(sendTransactionAmount.getText())
                    ).toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            gp1.load(0,3,getGlobalValueLabel).removeScaleY().removeScaleX().add();
            gp1.load(1,3,getGlobalValueExec).setWidth(2).removeScaleY().removeScaleX().add();
            getGlobalValueExec.addActionListener(e ->
                    result.setText(LedgerClient.getGlobalValue().toString()));

            gp1.load(0,4,getExtractLabel).removeScaleY().removeScaleX().add();
            gp1.load(1,4,getExtractExec).setWidth(2).removeScaleY().removeScaleX().add();
            getExtractExec.addActionListener(e -> {
                try {
                    result.setText(LedgerClient.getExtract(
                            (String) wallets.getSelectedItem()).toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            gp1.load(0,5,getLedgerLabel).removeScaleY().removeScaleX().add();
            gp1.load(1,5,getLedgerExec).setWidth(2).removeScaleY().removeScaleX().add();
            getLedgerExec.addActionListener(e ->
                    result.setText(LedgerClient.getLedger().toString()));

            gp1.load(0,6,getTotalValueLabel).removeScaleY().removeScaleX().add();
            gp1.load(1,6,getTotalValueExec).setWidth(2).removeScaleY().removeScaleX().add();

            getTotalValueExec.addActionListener(e -> new SelectorPopUp("Wallet Selector", new ArrayList<>(LedgerClient.wallets.keySet())));
            gp1.setBorder(BorderFactory.createTitledBorder("Operations"));

            gp0.load(0,1,gp1).setWidth(4).add();

            gp0.load(0,2, result).setWidth(4).add();

            add(gp0, BorderLayout.CENTER);
        }
    }
    static class SelectorPopUp extends JFrame {
        public SelectorPopUp(String title, List<String> values) {
            setTitle(title);

            getContentPane().setLayout(new BorderLayout());

            JGridBagPanel top = new JGridBagPanel();
            top.load(0,0, new JSeparator(SwingConstants.HORIZONTAL)).add();
            top.load(0,1, Box.createRigidArea(new Dimension(0,20))).add();
            getContentPane().add(top, BorderLayout.PAGE_START);
            getContentPane().add(Box.createRigidArea(new Dimension(0,20)), BorderLayout.PAGE_END);
            getContentPane().add(Box.createRigidArea(new Dimension(20,0)), BorderLayout.LINE_START);
            getContentPane().add(Box.createRigidArea(new Dimension(20,0)), BorderLayout.LINE_END);
            getContentPane().add(new PopUpPanel(values), BorderLayout.CENTER);

            setSize(new Dimension(300, 300));
            setResizable(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }

        static class PopUpPanel extends JPanel {

            JList<String> values = new JList<>();
            JButton submit = new JButton("Submit");
            JButton cancel = new JButton("Cancel");

            public PopUpPanel(List<String> values){
                setLayout(new BorderLayout());
                JGridBagPanel gp0 = new JGridBagPanel();

                this.values.setListData(values.toArray(new String[0]));

                gp0.load(0, 0, this.values).setWidth(2).add();
                gp0.load(0, 1, cancel).removeScaleY().removeScaleX().setAnchorLeft().add();
                gp0.load(1, 1, submit).removeScaleX().removeScaleY().setAnchorRight().add();

                add(gp0, BorderLayout.CENTER);
            }
        }

    }
}
