package com.csd.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class JPromptTextField extends JTextField {

    public JPromptTextField(final String promptText) {
        super();
        setText(promptText);
        setForeground(new Color(255, 255, 255, 76));
        setHorizontalAlignment(JTextField.CENTER);

        addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                if(getText().isEmpty()) {
                    setText(promptText);
                    setForeground(new Color(255, 255, 255, 76));
                    setHorizontalAlignment(JTextField.CENTER);
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                if(getText().equals(promptText)) {
                    setText("");
                    setForeground(new Color(255, 255, 255, 255));
                    setHorizontalAlignment(JTextField.LEFT);
                }
            }
        });

    }

}
