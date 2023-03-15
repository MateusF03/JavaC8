package me.mateus.javac8;

import me.mateus.javac8.emulator.Chip8;
import me.mateus.javac8.emulator.Display;
import me.mateus.javac8.emulator.KeyPad;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class Chip8Frame extends JFrame {

    public Chip8Frame() {
        setResizable(false);
        setTitle("Chip-8 Emulator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        init();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void init() {
        Display display = new Display();
        KeyPad keyPad = new KeyPad();
        Chip8 emulator = new Chip8(this, display, keyPad);

        add(display);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Arquivo");
        JMenuItem openFile = new JMenuItem("Abrir");

        openFile.addActionListener(l -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Chip-8 ROMs", "ch8"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File romFile = fileChooser.getSelectedFile();
                try {
                    emulator.loadRom(romFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        fileMenu.add(openFile);
        fileMenu.addSeparator();

        JMenuItem exit = new JMenuItem("Sair");
        exit.addActionListener(l -> {
            emulator.setRunning(false);
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        fileMenu.add(exit);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

    }
}
