package me.mateus.javac8.emulator;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KeyPad implements KeyListener {

    private final boolean[] keyValues = new boolean[16];
    private final Map<Integer, Integer> keyMap = new HashMap<>();

    public KeyPad() {
        Arrays.fill(keyValues, false);

        keyMap.put(KeyEvent.VK_1, 0x1);
        keyMap.put(KeyEvent.VK_2, 0x2);
        keyMap.put(KeyEvent.VK_3, 0x3);
        keyMap.put(KeyEvent.VK_4, 0xC);

        keyMap.put(KeyEvent.VK_Q, 0x4);
        keyMap.put(KeyEvent.VK_W, 0x5);
        keyMap.put(KeyEvent.VK_E, 0x6);
        keyMap.put(KeyEvent.VK_R, 0xD);

        keyMap.put(KeyEvent.VK_A, 0x7);
        keyMap.put(KeyEvent.VK_S, 0x8);
        keyMap.put(KeyEvent.VK_D, 0x9);
        keyMap.put(KeyEvent.VK_F, 0xE);

        keyMap.put(KeyEvent.VK_Z, 0xA);
        keyMap.put(KeyEvent.VK_X, 0x0);
        keyMap.put(KeyEvent.VK_C, 0xB);
        keyMap.put(KeyEvent.VK_V, 0xF);
    }

    public boolean[] getKeyValues() {
        return keyValues;
    }

    public boolean getKeyValue(int key) {
        return keyValues[key];
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (!keyMap.containsKey(keyCode))
            return;
        keyValues[keyMap.get(keyCode)] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (!keyMap.containsKey(keyCode))
            return;
        keyValues[keyMap.get(keyCode)] = false;
    }
}
