package com.csd.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class JPromptTextField extends JTextField {

    String promptText;

    public JPromptTextField(final String promptText) {
        super();
        this.promptText = promptText;
        setText(promptText);
        setForeground(new Color(255, 255, 255, 76));
        setHorizontalAlignment(JTextField.CENTER);

        addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                if(getText().isEmpty()) prompt();
            }

            @Override
            public void focusGained(FocusEvent e) {
                if(isEmpty()) clear();
            }
        });

    }

    @Override
    public String getText() {
        if(isEmpty())
            return "";
        else
            return super.getText();
    }

    public void prompt() {
        setText(promptText);
        setForeground(new Color(255, 255, 255, 76));
        setHorizontalAlignment(JTextField.CENTER);
    }

    public void clear() {
        setText("");
        setForeground(new Color(255, 255, 255, 255));
        setHorizontalAlignment(JTextField.LEFT);
    }

    public boolean isEmpty() {
        return super.getText().equals(promptText);
    }
}
