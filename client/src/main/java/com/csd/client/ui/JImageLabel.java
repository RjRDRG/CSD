package com.csd.client.ui;

import javax.swing.*;
import java.awt.*;

public class JImageLabel extends JLabel {

    public JImageLabel(){
        super();
    }

    public void setIcon(ImageIcon icon) {
        int width = 1000;//this.getWidth();
        int height = 1000;//this.getHeight();
        icon.setImage(icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        super.setIcon(icon);
    }
}
