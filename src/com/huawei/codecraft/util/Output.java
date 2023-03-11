package com.huawei.codecraft.util;

import java.io.BufferedOutputStream;
import java.io.PrintStream;

public class Output {
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
        System.err.println(obj);
    }
}
