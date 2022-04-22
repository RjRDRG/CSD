package com.csd.client;

import com.csd.client.ui.JGridBagPanel;

import javax.swing.*;
import java.awt.*;

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

    class MainPanel extends JPanel {
        JLabel walletLabel = new JLabel("Wallet");
        JComboBox<String> wallets= new JComboBox<>();
        JTextField newWalletName = new JTextField();
        JButton createWalletButton = new JButton("New Wallet");

        JLabel loadMoneyLabel = new JLabel("Load Money");
        JTextField loadMoneyAmount = new JTextField();
        JButton loadMoneyExec = new JButton("Exec");

        JLabel getBalanceLabel = new JLabel("Get Balance");
        JButton getBalanceExec = new JButton("Exec");

        JTextArea result = new JTextArea();

        public MainPanel() {
            setLayout(new BorderLayout());
            JGridBagPanel gp0 = new JGridBagPanel();

            gp0.load(0,0,walletLabel).removeScaleY().removeScaleX().setRightPad(5).setAnchorLeft().add();
            gp0.load(1,0,wallets).removeScaleY().removeScaleX().setAnchorLeft().add();
            gp0.load(2,0,newWalletName).removeScaleY().setLeftPad(200).setAnchorRight().add();
            gp0.load(3,0,createWalletButton).removeScaleY().removeScaleX().setAnchorRight().add();

            JGridBagPanel gp1 = new JGridBagPanel();

            gp1.load(0,0,loadMoneyLabel).removeScaleY().add();
            gp1.load(1,0,loadMoneyAmount).removeScaleY().add();
            gp1.load(2,0,loadMoneyExec).removeScaleY().removeScaleX().add();

            gp1.load(0,1, getBalanceLabel).removeScaleY().removeScaleX().add();
            gp1.load(1,1, getBalanceExec).setWidth(2).removeScaleY().removeScaleX().add();

            gp1.setBorder(BorderFactory.createTitledBorder("Operations"));

            gp0.load(0,1,gp1).setWidth(4).add();

            gp0.load(0,2, result).setWidth(4).add();

            add(gp0, BorderLayout.CENTER);
        }
    }
}
