package com.csd.client;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class RSyntaxConsole extends RSyntaxTextArea implements IConsole {

    public RSyntaxConsole() {
        super();
    }

    @Override
    public void printOperation(String request, String result) {
        append(request + "\n");
        append(result + "\n\n\n");
    }
}
