package me.mateus.javac8.emulator;

import javax.swing.*;
import java.awt.*;

public class Display extends JPanel {

    private final int[][] displayValues = new int[64][32];

    public Display() {
        clear();
        setPreferredSize(new Dimension(640, 320));
    }

    public void clear() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                displayValues[x][y] = 0;
            }
        }
    }

    public int getValue(int x, int y) {
        return displayValues[x][y];
    }

    public void setValue(int x, int y) {
        displayValues[x][y] ^= 1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 640, 320);
        g.setColor(Color.WHITE);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                if (displayValues[x][y] == 0) continue;
                g.fillRect(x * 10, y * 10, 10, 10);
            }
        }
    }
}
