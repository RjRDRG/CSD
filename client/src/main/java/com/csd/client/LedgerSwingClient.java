package com.csd.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.csd.client.ui.JGridBagPanel;
import com.csd.client.ui.JPromptTextField;
import com.csd.common.traits.Result;
import com.csd.common.util.Status;
import com.formdev.flatlaf.FlatDarculaLaf;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class LedgerSwingClient extends JFrame{

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        LedgerClient.createWallet("default@csd.com", UUID.randomUUID().toString(), null);

        FlatDarculaLaf.setup();
        new LedgerSwingClient();
    }

    public LedgerSwingClient() throws InterruptedException {
        JPanel mainPanel = new MainPanel();

        setTitle("Wallet");

        getContentPane().setLayout(new BorderLayout());

        JLabel loading = new JLabel(new ImageIcon("client/src/main/resources/images/load.gif"));
        getContentPane().add(loading, BorderLayout.CENTER);

        setSize(new Dimension(1000, 1000));
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        Thread.sleep(1500);
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
        JButton createWalletButton = new JButton("New");

        JLabel changeProxyLabel = new JLabel("Proxy");
        JComboBox<String> changeProxy = new JComboBox<>(LedgerClient.proxyPorts);

        JLabel loadMoneyLabel = new JLabel("Load Money");
        JPromptTextField loadMoneyAmount = new JPromptTextField("Amount");
        JButton loadMoneyExec = new JButton("Execute");

        JLabel getBalanceLabel = new JLabel("Get Balance");
        JButton getBalanceExec = new JButton("Execute");

        JLabel getEncryptedBalanceLabel = new JLabel("Get Encrypted Balance");
        JButton getEncryptedBalanceExec = new JButton("Execute");

        JLabel sendTransactionLabel = new JLabel("Send Transaction");
        JComboBox<String> sendTransactionDestination = new JComboBox<>(LedgerClient.wallets.keySet().toArray(new String[0]));
        JPromptTextField sendTransactionAmount = new JPromptTextField("Amount");

        JPromptTextField sendTransactionFee = new JPromptTextField("Fee");
        JButton sendTransactionExec = new JButton("Execute");

        JLabel storeTransactionLabel = new JLabel("Store Transaction");
        JComboBox<String> storeTransactionDestination = new JComboBox<>(LedgerClient.wallets.keySet().toArray(new String[0]));
        JPromptTextField storeTransactionAmount = new JPromptTextField("Amount");

        JPromptTextField storeTransactionFee = new JPromptTextField("Fee");
        JButton storeTransactionExec = new JButton("Execute");

        JLabel sendStoredTransactionLabel = new JLabel("Send Stored Transaction");
        JComboBox<String> sendStoredTransactionDestination = new JComboBox<>(LedgerClient.storedTransactions.toArray(new String[0]));

        JCheckBox sendStoredTransactionIsPrivate = new JCheckBox("Is Private");
        JButton sendStoredTransactionExec = new JButton("Execute");

        JLabel decryptTransactionLabel = new JLabel("Decrypt Value Asset");
        JComboBox<String> decryptTransactionAsset = new JComboBox<>(LedgerClient.tokens.keySet().toArray(new String[0]));

        JPromptTextField decryptTransactionFee = new JPromptTextField("Fee");
        JButton decryptTransactionExec = new JButton("Execute");

        JLabel getGlobalValueLabel = new JLabel("Get Global Value");
        JButton getGlobalValueExec = new JButton("Execute");

        JLabel getExtractLabel = new JLabel("Get Extract");
        JButton getExtractExec = new JButton("Execute");

        JLabel getLedgerLabel = new JLabel("Get Ledger");
        JButton getLedgerExec = new JButton("Execute");

        JLabel getTotalValueLabel = new JLabel("Get Total Value");
        JButton getTotalValueExec = new JButton("Execute");

        JLabel resultLabel = new JLabel("Result");
        RSyntaxConsole result = new RSyntaxConsole();

        public MainPanel() {
            setLayout(new BorderLayout());
            JGridBagPanel gp0 = new JGridBagPanel();

            gp0.load(0,0,walletLabel).removeScaleY().removeScaleX().setRightPad(5).add();
            wallets.setPreferredSize(new Dimension(300, wallets.getPreferredSize().height));
            gp0.load(1,0,wallets).removeScaleY().removeScaleX().add();
            gp0.load(2,0,createWalletButton).removeScaleY().removeScaleX().add();
            createWalletButton.addActionListener(e -> new NewWalletPopUp((n,s) -> {
                LedgerClient.createWallet(n, s, result);
                wallets.addItem(n);
                wallets.setSelectedItem(n);
                sendTransactionDestination.addItem(n);
                storeTransactionDestination.addItem(n);
            }));


            JGridBagPanel gpAux = new JGridBagPanel();
            gpAux.load(0,0,changeProxyLabel).removeScaleY().removeScaleX().add();
            gpAux.load(1,0,changeProxy).removeScaleY().removeScaleX().add();
            changeProxy.addActionListener(e -> LedgerClient.changeProxy((String) changeProxy.getSelectedItem()));

            gp0.load(3,0,gpAux).removeScaleY().removeScaleX().setAnchorRight().add();

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
            gp1.load(4,0,loadMoneyExec).removeScaleX().removeScaleY().add();
            loadMoneyExec.addActionListener(e -> {
                try {
                    LedgerClient.loadMoney((String) wallets.getSelectedItem(), Double.parseDouble(loadMoneyAmount.getText()), result);
                } catch (Exception exception) {
                    result.append(Result.error(Status.BAD_REQUEST, exception.getClass().getSimpleName() + ": " + exception.getMessage()) + "\n\n\n");
                }
            });

            gp1.load(0,1,getBalanceLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(4,1,getBalanceExec).removeScaleX().removeScaleY().add();
            getBalanceExec.addActionListener(e ->
                LedgerClient.getBalance((String) wallets.getSelectedItem(), result)
            );

            gp1.load(0,2,getEncryptedBalanceLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(4,2,getEncryptedBalanceExec).removeScaleX().removeScaleY().add();
            getEncryptedBalanceExec.addActionListener(e ->
                    LedgerClient.getEncryptedBalance((String) wallets.getSelectedItem(), result)
            );

            gp1.load(0,3,sendTransactionLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(1,3,sendTransactionDestination).setLeftPad(10).removeScaleY().setWeight(0.95f,1f).add();
            gp1.load(2,3,sendTransactionAmount).removeScaleY().add();
            gp1.load(3,3,sendTransactionFee).removeScaleY().add();
            gp1.load(4,3,sendTransactionExec).removeScaleX().removeScaleY().add();
            sendTransactionExec.addActionListener(e -> {
                try {
                    LedgerClient.sendTransaction(
                            (String) wallets.getSelectedItem(),
                            (String) sendTransactionDestination.getSelectedItem(),
                            Double.parseDouble(sendTransactionAmount.getText()),
                            Double.parseDouble(sendTransactionFee.getText()),
                            result
                    );
                } catch (Exception exception) {
                    result.append(Result.error(Status.BAD_REQUEST, exception.getClass().getSimpleName() + ": " + exception.getMessage()) + "\n\n\n");
                }
            });

            gp1.load(0,4,storeTransactionLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(1,4,storeTransactionDestination).setLeftPad(10).removeScaleY().setWeight(0.95f,1f).add();
            gp1.load(2,4,storeTransactionAmount).removeScaleY().add();
            gp1.load(3,4,storeTransactionFee).removeScaleY().add();
            gp1.load(4,4,storeTransactionExec).removeScaleX().removeScaleY().add();
            storeTransactionExec.addActionListener(e -> {
                try {
                    String fileName = LedgerClient.storeTransaction(
                            (String) wallets.getSelectedItem(),
                            (String) storeTransactionDestination.getSelectedItem(),
                            Double.parseDouble(storeTransactionAmount.getText()),
                            Double.parseDouble(storeTransactionFee.getText()),
                            result
                    );
                    sendStoredTransactionDestination.addItem(fileName);
                    sendStoredTransactionDestination.setSelectedItem(fileName);
                } catch (Exception exception) {
                    result.append(Result.error(Status.BAD_REQUEST, exception.getClass().getSimpleName() + ": " + exception.getMessage()) + "\n\n\n");
                }
            });

            gp1.load(0,5,sendStoredTransactionLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(1,5,sendStoredTransactionDestination).setLeftPad(10).removeScaleY().setWeight(0.95f,1f).add();
            gp1.load(2,5,sendStoredTransactionIsPrivate).setAnchorRight().removeScaleY().add();
            gp1.load(4,5,sendStoredTransactionExec).removeScaleX().removeScaleY().add();
            sendStoredTransactionExec.addActionListener(e -> {
                try {
                    String asset = LedgerClient.sendStoredTransaction(
                            (String) wallets.getSelectedItem(),
                            (String) sendStoredTransactionDestination.getSelectedItem(),
                            sendStoredTransactionIsPrivate.isSelected(),
                            result
                    );
                    if(asset != null) {
                        decryptTransactionAsset.addItem(asset);
                        decryptTransactionAsset.setSelectedItem(asset);
                    }
                } catch (Exception exception) {
                    result.append(Result.error(Status.BAD_REQUEST, exception.getClass().getSimpleName() + ": " + exception.getMessage()) + "\n\n\n");
                }
            });

            gp1.load(0,6,decryptTransactionLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(1,6,decryptTransactionAsset).setLeftPad(10).removeScaleY().setWeight(0.95f,1f).add();
            gp1.load(3,6,decryptTransactionFee).removeScaleY().add();
            gp1.load(4,6,decryptTransactionExec).removeScaleX().removeScaleY().add();
            decryptTransactionExec.addActionListener(e -> {
                try {
                    LedgerClient.decryptValueAsset(
                            (String) wallets.getSelectedItem(),
                            (String) decryptTransactionAsset.getSelectedItem(),
                            Double.parseDouble(storeTransactionFee.getText()),
                            result
                    );
                } catch (Exception exception) {
                    result.append(Result.error(Status.BAD_REQUEST, exception.getClass().getSimpleName() + ": " + exception.getMessage()) + "\n\n\n");
                }
            });

            gp1.load(0,7,getGlobalValueLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(4,7,getGlobalValueExec).removeScaleX().removeScaleY().add();
            getGlobalValueExec.addActionListener(e -> LedgerClient.getGlobalValue(result));

            gp1.load(0,8,getExtractLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(4,8,getExtractExec).removeScaleX().removeScaleY().add();
            getExtractExec.addActionListener(e -> LedgerClient.getExtract((String) wallets.getSelectedItem(), result));

            gp1.load(0,9,getLedgerLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(4,9,getLedgerExec).removeScaleX().removeScaleY().add();
            getLedgerExec.addActionListener(e -> LedgerClient.getLedger(result));

            gp1.load(0,10,getTotalValueLabel).removeScaleX().removeScaleY().setAnchorLeft().setRightPad(5).add();
            gp1.load(4,10,getTotalValueExec).removeScaleX().removeScaleY().add();
            getTotalValueExec.addActionListener(e -> new SelectorPopUp("Wallet Selector", new ArrayList<>(LedgerClient.wallets.keySet()), result));

            border.add(gp1, BorderLayout.CENTER);

            gp0.load(0,1, border).setWidth(4).removeScaleY().setTopPad(10).add();

            gp0.load(0,2, resultLabel).setWidth(4).setTopPad(5).removeScaleY().add();

            result.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            try {
                Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
                theme.apply(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            gp0.load(0,3, new RTextScrollPane(result)).setWidth(4).add();

            add(gp0, BorderLayout.CENTER);
        }
    }


    static class SelectorPopUp extends JFrame {
        public SelectorPopUp(String title, List<String> values, IConsole console) {
            setTitle(title);

            getContentPane().setLayout(new BorderLayout());

            JGridBagPanel top = new JGridBagPanel();
            top.load(0,0, new JSeparator(SwingConstants.HORIZONTAL)).add();
            top.load(0,1, Box.createRigidArea(new Dimension(0,10))).add();
            getContentPane().add(top, BorderLayout.PAGE_START);
            getContentPane().add(Box.createRigidArea(new Dimension(0,10)), BorderLayout.PAGE_END);
            getContentPane().add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.LINE_START);
            getContentPane().add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.LINE_END);
            getContentPane().add(new PopUpPanel(values, console), BorderLayout.CENTER);

            setSize(new Dimension(300, 300));
            setLocationRelativeTo(null);
            setResizable(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }

        class PopUpPanel extends JPanel {

            JList<String> options = new JList<>();
            JButton submit = new JButton("Submit");

            public PopUpPanel(List<String> values, IConsole console){
                setLayout(new BorderLayout());
                JGridBagPanel gp0 = new JGridBagPanel();

                this.options.setListData(values.toArray(new String[0]));

                gp0.load(0, 0, new JScrollPane(options)).setWidth(2).add();

                submit.addActionListener(e -> {
                    JList<String> optionsRef = options;
                    SelectorPopUp.this.dispose();
                    LedgerClient.getTotalValue(optionsRef.getSelectedValuesList(), console);
                });

                gp0.load(1, 1, submit).removeScaleX().removeScaleY().setTopPad(5).setAnchorRight().add();

                add(gp0, BorderLayout.CENTER);
            }
        }

    }

    static class NewWalletPopUp extends JFrame {
        public NewWalletPopUp(BiConsumer<String,String> consumer) {
            setTitle("New Wallet");

            getContentPane().setLayout(new BorderLayout());

            JGridBagPanel top = new JGridBagPanel();
            top.load(0,0, new JSeparator(SwingConstants.HORIZONTAL)).add();
            top.load(0,1, Box.createRigidArea(new Dimension(0,10))).add();
            getContentPane().add(top, BorderLayout.PAGE_START);
            getContentPane().add(Box.createRigidArea(new Dimension(0,10)), BorderLayout.PAGE_END);
            getContentPane().add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.LINE_START);
            getContentPane().add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.LINE_END);
            getContentPane().add(new PopUpPanel(consumer), BorderLayout.CENTER);

            setSize(new Dimension(300, 300));
            setLocationRelativeTo(null);
            setResizable(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }

        class PopUpPanel extends JPanel {

            JLabel emailLabel = new JLabel("Email");
            JTextField email = new JTextField();
            JLabel seedLabel = new JLabel("Seed");
            JLabel seed = new JLabel(UUID.randomUUID().toString());
            JButton submit = new JButton("Submit");

            public PopUpPanel(BiConsumer<String,String> consumer){
                setLayout(new BorderLayout());
                JGridBagPanel gp0 = new JGridBagPanel();

                gp0.load(0, 0, emailLabel).removeScaleY().add();
                gp0.load(1, 0, email).removeScaleY().add();

                gp0.load(0, 1, seedLabel).removeScaleY().add();
                gp0.load(1, 1, seed).removeScaleY().add();

                submit.addActionListener(e -> {
                    if(!email.getText().isEmpty()) {
                        consumer.accept(email.getText(), seed.getText());
                        NewWalletPopUp.this.dispose();
                    }
                });

                add(gp0, BorderLayout.PAGE_START);

                JGridBagPanel gp1 = new JGridBagPanel();
                gp1.load(0, 0, Box.createRigidArea(new Dimension(10,10))).removeScaleY().setAnchorRight().add();
                gp1.load(1, 0, submit).removeScaleX().removeScaleY().setAnchorRight().add();
                add(gp1, BorderLayout.PAGE_END);
            }
        }

    }
}
