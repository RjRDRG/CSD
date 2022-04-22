package com.csd.client;

import com.csd.client.ui.JGridBagPanel;
import com.csd.client.ui.JPromptTextField;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
        JComboBox<String> wallets= new JComboBox<>(LedgerClient.wallets.keySet().toArray(new String[0]));
        JTextField newWalletName = new JTextField();
        JButton createWalletButton = new JButton("New Wallet");

        JLabel loadMoneyLabel = new JLabel("Load Money");
        JPromptTextField loadMoneyAmount = new JPromptTextField("Amount");
        JButton loadMoneyExec = new JButton("Execute");

        JLabel getBalanceLabel = new JLabel("Get Balance");
        JButton getBalanceExec = new JButton("Execute");

        JLabel sendTransactionLabel = new JLabel("Send Transaction");
        JComboBox<String> sendTransactionDestination = new JComboBox<>();
        JPromptTextField sendTransactionAmount = new JPromptTextField("Amount");
        JButton sendTransactionExec = new JButton("Execute");

        JLabel getGlobalValueLabel = new JLabel("Get Global Value");
        JButton getGlobalValueExec = new JButton("Execute");

        JLabel getExtractLabel = new JLabel("Get Extract");
        JButton getExtractExec = new JButton("Execute");

        JLabel getLedgerLabel = new JLabel("Get Ledger");
        JButton getLedgerExec = new JButton("Execute");

        JLabel getTotalValueLabel = new JLabel("Get Total Value");
        JButton getTotalValueExec = new JButton("Execute");

        JLabel resultLabel = new JLabel("Result");
        RSyntaxTextArea result = new RSyntaxTextArea();

        public MainPanel() {
            setLayout(new BorderLayout());
            JGridBagPanel gp0 = new JGridBagPanel();

            gp0.load(0,0,walletLabel).removeScaleY().removeScaleX().setAnchorLeft().setRightPad(5).add();
            gp0.load(1,0,wallets).removeScaleY().removeScaleX().setAnchorLeft().add();
            gp0.load(2,0,newWalletName).removeScaleY().setLeftPad(200).setAnchorRight().add();
            gp0.load(3,0,createWalletButton).removeScaleY().removeScaleX().setAnchorRight().add();
            createWalletButton.addActionListener(e -> {
                try {
                    LedgerClient.wallets.put(newWalletName.getText(), new WalletDetails());
                    wallets.addItem(newWalletName.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            JGridBagPanel gp1 = new JGridBagPanel();

            //components of op
            gp1.load(0,0,loadMoneyLabel).add();
            loadMoneyAmount.setToolTipText("Amount");
            gp1.load(1,0,loadMoneyAmount).add();
            gp1.load(3,0,loadMoneyExec).add();
            loadMoneyExec.addActionListener(e ->
                result.append(LedgerClient.loadMoney(
                    (String) wallets.getSelectedItem(),
                    Double.parseDouble(loadMoneyAmount.getText())
                ).toString() + "\n\n\n")
            );

            gp1.load(0,1,getBalanceLabel).add();
            gp1.load(3,1,getBalanceExec).add();
            getBalanceExec.addActionListener(e ->
                result.append(LedgerClient.getBalance(
                        (String) wallets.getSelectedItem()
                ).toString() + "\n\n\n")
            );

            gp1.load(0,2,sendTransactionLabel).add();
            gp1.load(1,2, sendTransactionDestination).add();
            gp1.load(2,2,sendTransactionAmount).add();
            gp1.load(3,2,sendTransactionExec).add();
            sendTransactionExec.addActionListener(e ->
                result.append(LedgerClient.sendTransaction(
                    (String) wallets.getSelectedItem(),
                    (String) sendTransactionDestination.getSelectedItem(),
                    Double.parseDouble(sendTransactionAmount.getText())
                ).toString() + "\n\n\n")
            );

            gp1.load(0,3,getGlobalValueLabel).add();
            gp1.load(3,3,getGlobalValueExec).add();
            getGlobalValueExec.addActionListener(e -> result.append(LedgerClient.getGlobalValue().toString() + "\n\n\n"));

            gp1.load(0,4,getExtractLabel).add();
            gp1.load(3,4,getExtractExec).add();
            getExtractExec.addActionListener(e -> result.append(LedgerClient.getExtract((String) wallets.getSelectedItem()).toString() + "\n\n\n" ));

            gp1.load(0,5,getLedgerLabel).add();
            gp1.load(3,5,getLedgerExec).add();
            getLedgerExec.addActionListener(e -> result.append(LedgerClient.getLedger().toString() + "\n\n\n" ));

            gp1.load(0,6,getTotalValueLabel).add();
            gp1.load(3,6,getTotalValueExec).add();
            getTotalValueExec.addActionListener(e -> new SelectorPopUp("Wallet Selector", new ArrayList<>(LedgerClient.wallets.keySet())));

            gp1.setBorder(BorderFactory.createTitledBorder("Operations"));

            gp0.load(0,1,gp1).setWidth(4).setTopPad(10).add();

            gp0.load(0,2, resultLabel).setWidth(4).setTopPad(5).removeScaleY().add();

            result.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            try {
                Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
                theme.apply(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            gp0.load(0,3, new RTextScrollPane(result)).setWidth(4).add();

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
