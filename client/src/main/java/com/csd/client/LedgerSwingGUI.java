package com.csd.client;

import com.csd.client.ui.JGridBagPanel;
import com.csd.client.ui.JPromptTextField;
import com.csd.common.traits.Result;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LedgerSwingGUI extends JFrame{

    public LedgerSwingGUI() throws InterruptedException {
        JPanel mainPanel = new MainPanel();

        setTitle("Wallet");

        getContentPane().setLayout(new BorderLayout());

        JLabel loading = new JLabel(new ImageIcon("client/src/main/resources/images/load.gif"));
        loading.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        getContentPane().add(loading, BorderLayout.CENTER);

        setSize(new Dimension(1000, 1000));
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        Thread.sleep(30000);
        getContentPane().remove(loading);

        JGridBagPanel top = new JGridBagPanel();
        top.load(0,0, new JSeparator(SwingConstants.HORIZONTAL)).add();
        top.load(0,1, Box.createRigidArea(new Dimension(0,20))).add();
        getContentPane().add(top, BorderLayout.PAGE_START);
        getContentPane().add(Box.createRigidArea(new Dimension(0,20)), BorderLayout.PAGE_END);
        getContentPane().add(Box.createRigidArea(new Dimension(20,0)), BorderLayout.LINE_START);
        getContentPane().add(Box.createRigidArea(new Dimension(20,0)), BorderLayout.LINE_END);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    static class MainPanel extends JPanel {
        JLabel walletLabel = new JLabel("Wallet");
        JComboBox<String> wallets= new JComboBox<>(LedgerClient.wallets.keySet().toArray(new String[0]));
        JPromptTextField newWalletName = new JPromptTextField("Name");
        JButton createWalletButton = new JButton("New Wallet");

        JLabel loadMoneyLabel = new JLabel("Load Money");
        JPromptTextField loadMoneyAmount = new JPromptTextField("Amount");
        JButton loadMoneyExec = new JButton("Execute");

        JLabel getBalanceLabel = new JLabel("Get Balance");
        JButton getBalanceExec = new JButton("Execute");

        JLabel sendTransactionLabel = new JLabel("Send Transaction");
        JComboBox<String> sendTransactionDestination = new JComboBox<>(LedgerClient.wallets.keySet().toArray(new String[0]));
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
            gp0.load(1,0,wallets).removeScaleY().setWeight(0.15f,1f).setAnchorLeft().add();
            gp0.load(2,0,Box.createRigidArea(new Dimension(0,0))).removeScaleY().setWeight(0.55f,1f).setAnchorLeft().add();
            gp0.load(3,0,newWalletName).removeScaleY().setWeight(0.3f,1f).setAnchorRight().add();
            gp0.load(4,0,createWalletButton).removeScaleY().removeScaleX().setAnchorRight().add();
            createWalletButton.addActionListener(e -> {
                try {
                    if(!newWalletName.isEmpty()) {
                        LedgerClient.wallets.put(newWalletName.getText(), new WalletDetails());
                        wallets.addItem(newWalletName.getText());
                        wallets.setSelectedItem(newWalletName.getText());
                        sendTransactionDestination.addItem(newWalletName.getText());
                        newWalletName.prompt();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            JPanel border = new JPanel();
            border.setLayout(new BorderLayout());
            border.setBorder(BorderFactory.createTitledBorder("Operations"));
            border.add(Box.createRigidArea(new Dimension(0,10)), BorderLayout.PAGE_END);
            border.add(Box.createRigidArea(new Dimension(0,10)), BorderLayout.PAGE_START);
            border.add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.LINE_START);
            border.add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.LINE_END);

            JGridBagPanel gp1 = new JGridBagPanel();

            gp1.load(0,0,loadMoneyLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            loadMoneyAmount.setToolTipText("Amount");
            gp1.load(2,0,loadMoneyAmount).removeScaleY().add();
            gp1.load(3,0,loadMoneyExec).removeScaleX().removeScaleY().add();
            loadMoneyExec.addActionListener(e -> {
                try {
                    result.append(LedgerClient.loadMoney(
                            (String) wallets.getSelectedItem(),
                            Double.parseDouble(loadMoneyAmount.getText())
                    ).toString() + "\n\n\n");
                } catch (Exception exception) {
                    result.append(Result.error(Result.Status.BAD_REQUEST, exception.getClass().getSimpleName() + ": " + exception.getMessage()) + "\n\n\n");
                }
            });

            gp1.load(0,1,getBalanceLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(3,1,getBalanceExec).removeScaleX().removeScaleY().add();
            getBalanceExec.addActionListener(e ->
                result.append(LedgerClient.getBalance(
                        (String) wallets.getSelectedItem()
                ).toString() + "\n\n\n")
            );

            gp1.load(0,2,sendTransactionLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(1,2,sendTransactionDestination).setLeftPad(10).removeScaleY().setWeight(0.95f,1f).add();
            gp1.load(2,2,sendTransactionAmount).removeScaleY().setWeight(0.05f,1f).add();
            gp1.load(3,2,sendTransactionExec).removeScaleX().removeScaleY().add();
            sendTransactionExec.addActionListener(e -> {
                try {
                    result.append(LedgerClient.sendTransaction(
                            (String) wallets.getSelectedItem(),
                            (String) sendTransactionDestination.getSelectedItem(),
                            Double.parseDouble(sendTransactionAmount.getText())
                    ).toString() + "\n\n\n");
                } catch (Exception exception) {
                    result.append(Result.error(Result.Status.BAD_REQUEST, exception.getClass().getSimpleName() + ": " + exception.getMessage()) + "\n\n\n");
                }
            });

            gp1.load(0,3,getGlobalValueLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(3,3,getGlobalValueExec).removeScaleX().removeScaleY().add();
            getGlobalValueExec.addActionListener(e -> result.append(LedgerClient.getGlobalValue().toString() + "\n\n\n"));

            gp1.load(0,4,getExtractLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(3,4,getExtractExec).removeScaleX().removeScaleY().add();
            getExtractExec.addActionListener(e -> result.append(LedgerClient.getExtract((String) wallets.getSelectedItem()).toString() + "\n\n\n" ));

            gp1.load(0,5,getLedgerLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(3,5,getLedgerExec).removeScaleX().removeScaleY().add();
            getLedgerExec.addActionListener(e -> result.append(LedgerClient.getLedger().toString() + "\n\n\n" ));

            gp1.load(0,6,getTotalValueLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(3,6,getTotalValueExec).removeScaleX().removeScaleY().add();
            getTotalValueExec.addActionListener(e -> new SelectorPopUp("Wallet Selector", new ArrayList<>(LedgerClient.wallets.keySet())));
            //TODO

            border.add(gp1, BorderLayout.CENTER);

            gp0.load(0,1, border).setWidth(5).removeScaleY().setTopPad(10).add();

            gp0.load(0,2, resultLabel).setWidth(5).setTopPad(5).removeScaleY().add();

            result.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            try {
                Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
                theme.apply(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            gp0.load(0,3, new RTextScrollPane(result)).setWidth(5).add();

            add(gp0, BorderLayout.CENTER);
        }
    }
    static class SelectorPopUp extends JFrame {
        public SelectorPopUp(String title, List<String> values) {
            setTitle(title);

            getContentPane().setLayout(new BorderLayout());

            JGridBagPanel top = new JGridBagPanel();
            top.load(0,0, new JSeparator(SwingConstants.HORIZONTAL)).add();
            top.load(0,1, Box.createRigidArea(new Dimension(0,10))).add();
            getContentPane().add(top, BorderLayout.PAGE_START);
            getContentPane().add(Box.createRigidArea(new Dimension(0,10)), BorderLayout.PAGE_END);
            getContentPane().add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.LINE_START);
            getContentPane().add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.LINE_END);
            getContentPane().add(new PopUpPanel(values), BorderLayout.CENTER);

            setSize(new Dimension(300, 300));
            setLocationRelativeTo(null);
            setResizable(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }

        static class PopUpPanel extends JPanel {

            JList<String> options = new JList<>();
            JButton submit = new JButton("Submit");

            public PopUpPanel(List<String> values){
                setLayout(new BorderLayout());
                JGridBagPanel gp0 = new JGridBagPanel();

                this.options.setListData(values.toArray(new String[0]));

                gp0.load(0, 0, new JScrollPane(options)).setWidth(2).add();
                gp0.load(1, 1, submit).removeScaleX().removeScaleY().setTopPad(5).setAnchorRight().add();

                add(gp0, BorderLayout.CENTER);
            }
        }

    }
}
