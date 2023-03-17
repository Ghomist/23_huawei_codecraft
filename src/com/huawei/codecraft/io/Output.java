package com.huawei.codecraft.io;

import java.io.BufferedOutputStream;
import java.io.PrintStream;

public class Output {
    public static final boolean DEBUG_MODE = false;

    public static final PrintStream out = new PrintStream(new BufferedOutputStream(System.out));

    public static void ok() {
        sendAndFlush("OK");
    }

    public static void sendAndFlush(Object obj) {
        out.println(obj);
        out.flush();
    }

    public static void send(Object obj) {
        out.println(obj);
    }

    public static void debug(Object obj) {
        if (!DEBUG_MODE)
            System.err.println(obj);
    }
}
