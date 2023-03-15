package me.mateus.javac8.emulator;

import me.mateus.javac8.Chip8Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

public class Chip8 {

    private final int[] memory = new int[4096];
    private final int[] v = new int[16];
    private final boolean[] keyCheck = new boolean[16];
    private final Random rnd = new Random();
    private final Deque<Integer> subRoutines = new ArrayDeque<>();
    private final int VF = 0xF;
    private final Chip8Frame frame;
    private final Display display;
    private final KeyPad keyPad;
    private final Thread timerThread;
    private final Thread emulationThread;
    private boolean running = true;
    private int iRegister = 0;
    private int pc = 0x200;
    private int delayTimer = 0;
    private int soundTimer = 0;

    public Chip8(Chip8Frame frame, Display display, KeyPad keyPad) {
        this.frame = frame;
        this.display = display;
        this.keyPad = keyPad;

        Arrays.fill(keyCheck, false);

        this.timerThread = new Thread(() -> {
            long taskTime;
            long sleepTime = 1000L / 60L;
            while (running) {
                taskTime = System.currentTimeMillis();
                if (delayTimer > 0)
                    delayTimer--;
                if (soundTimer > 0)
                    soundTimer--;
                taskTime = System.currentTimeMillis() - taskTime;
                if (sleepTime - taskTime > 0) {
                    try {
                        Thread.sleep(sleepTime - taskTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        this.emulationThread = new Thread(() -> {
            frame.addKeyListener(keyPad);
            long taskTime;
            long sleepTime = 1000L / 700L;
            while (running) {
                taskTime = System.currentTimeMillis();
                int opcode = fetch();
                decode(opcode);
                taskTime = System.currentTimeMillis() - taskTime;
                if (sleepTime - taskTime > 0) {
                    try {
                        Thread.sleep(sleepTime - taskTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        int[] font = {
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };
        System.arraycopy(font, 0, memory, 0, font.length);
    }

    public void loadRom(File romFile) throws IOException {
        byte[] b = Files.readAllBytes(romFile.toPath());
        for (int i = 0; i < b.length; i++) {
            memory[i + 0x200] = Byte.toUnsignedInt(b[i]);
        }
        start();
    }

    private void start() {
        timerThread.start();
        emulationThread.start();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private int fetch() {
        int value = memory[pc] << 8 | memory[pc + 1];
        pc += 2;
        return value;
    }

    private void decode(int opcode) {
        int x = (opcode & 0xF00) >> 8;
        int y = (opcode & 0xF0) >> 4;
        int n = opcode & 0xF;
        int nn = opcode & 0xFF;
        int nnn = opcode & 0xFFF;

        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode & 0xFF) {
                    case 0xE0:
                        display.clear();
                        display.repaint();
                        break;
                    case 0xEE:
                        pc = subRoutines.removeFirst();
                        break;
                }
                break;
            case 0x1000:
                pc = nnn;
                break;
            case 0x2000:
                subRoutines.addFirst(pc);
                pc = nnn;
                break;
            case 0x3000:
                if (v[x] == nn) {
                    pc += 2;
                }
                break;
            case 0x4000:
                if (v[x] != nn) {
                    pc += 2;
                }
                break;
            case 0x5000:
                if (v[x] == v[y]) {
                    pc += 2;
                }
                break;
            case 0x6000:
                v[x] = nn;
                break;
            case 0x7000:
                v[x] += nn;
                v[x] &= 255;
                break;
            case 0x8000:
                switch (opcode & 0xF) {
                    case 0x0:
                        v[x] = v[y];
                        break;
                    case 0x1:
                        //v[VF] = 0;
                        v[x] |= v[y];
                        v[VF] = 0;
                        break;
                    case 0x2:
                        //v[VF] = 0;
                        v[x] &= v[y];
                        v[VF] = 0;
                        break;
                    case 0x3:
                        //v[VF] = 0;
                        v[x] ^= v[y];
                        v[VF] = 0;
                        break;
                    case 0x4:
                        v[x] += v[y];
                        v[VF] = 0;
                        if (v[x] > 255) {
                            v[VF] = 1;
                        }
                        v[x] &= 255;
                        break;
                    case 0x5:
                        v[x] -= v[y];
                        v[x] &= 255;
                        v[VF] = 1;
                        if (v[x] < v[y]) {
                            v[VF] = 0;
                        }
                        break;
                    case 0x6:
                        v[x] = v[y];
                        int bit = v[x] & 0x1;
                        v[x] >>= 1;
                        v[VF] = bit;
                        break;
                    case 0x7:
                        v[x] = v[y] - v[x];
                        v[x] &= 255;
                        v[VF] = 1;
                        if (v[x] > v[y]) {
                            v[VF] = 0;
                        }
                        break;
                    case 0xE:
                        v[x] = v[y];
                        v[x] = (v[x] << 1) & 0xFF;
                        v[VF] = (v[x] & 0x80) >> 7;
                        break;
                }
                break;
            case 0x9000:
                if (v[x] != v[y]) {
                    pc += 2;
                }
                break;
            case 0xA000:
                iRegister = nnn;
                break;
            case 0xB000:
                pc = nnn + v[0];
                break;
            case 0xC000:
                v[x] = rnd.nextInt(256) & nn;
                break;
            case 0xD000:
                int xCord = v[x] % 64;
                int yCord = v[y] % 32;
                v[VF] = 0;
                for (int i = 0; i < n; i++) {
                    int spriteData = memory[iRegister + i];
                    for (int j = 0; j < 8; j++) {
                        if (xCord + j >= 64 | yCord + i >= 32)
                            break;
                        int pixelData = spriteData & (0x80 >> j);
                        if (pixelData == 0)
                            continue;
                        if (display.getValue(xCord + j, yCord + i) == 1)
                            v[VF] = 1;
                        display.setValue(xCord + j, yCord + i);
                    }
                }

                try {
                    Thread.sleep(16L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                display.repaint();
                break;
            case 0xE000:
                switch (opcode & 0xFF) {
                    case 0x9E:
                        if (keyPad.getKeyValue(v[x]))
                            pc += 2;
                        break;
                    case 0xA1:
                        if (!keyPad.getKeyValue(v[x]))
                            pc += 2;
                        break;
                }
                break;
            case 0xF000:
                switch (opcode & 0xFF) {
                    case 0x7:
                        v[x] = delayTimer;
                        break;
                    case 0x15:
                        delayTimer = v[x];
                        break;
                    case 0x18:
                        soundTimer = v[x];
                        break;
                    case 0x1E:
                        int sum = iRegister + v[x];
                        if (sum > 0x1000) {
                            v[VF] = 1;
                        }
                        iRegister = sum;
                        break;
                    case 0xA:
                        boolean[] keyValues = keyPad.getKeyValues();
                        boolean pressed = false;
                        for (int i = 0; i < keyValues.length; i++) {
                            if (keyCheck[i] && !keyValues[i]) {
                                v[x] = i;
                                pressed = true;
                                break;
                            }
                            if (keyValues[i]) {
                                keyCheck[i] = true;
                            }
                        }
                        if (!pressed) {
                            pc -= 2;
                        } else {
                            Arrays.fill(keyCheck, false);
                        }
                        break;
                    case 0x29:
                        iRegister = v[x] * 5;
                        break;
                    case 0x33:
                        memory[iRegister] = v[x] / 100;
                        memory[iRegister + 1] = (v[x] % 100) / 10;
                        memory[iRegister + 2] = (v[x] % 100) % 10;
                        break;
                    case 0x55:
                        for (int i = 0; i <= x; i++) {
                            memory[iRegister++] = v[i];
                        }
                        break;
                    case 0x65:
                        for (int i = 0; i <= x; i++) {
                            v[i] = memory[iRegister++];
                        }
                        break;
                }
                break;
        }
    }
}
