package com.huawei.codecraft.io;

import java.io.BufferedOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import com.huawei.codecraft.controller.GameController;

public class Output {
    public static final boolean DEBUG_MODE = true;

    public static final PrintStream out = new PrintStream(new BufferedOutputStream(System.out));
    public static FileWriter fw;

    static {
        try {
            fw = new FileWriter("log.txt");
        } catch (Exception e) {

        }
    }

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
        if (DEBUG_MODE) {
            System.err.println("[" + GameController.frameID + "] " + obj);
            try {
                fw.append("[" + GameController.frameID + "] " + obj.toString());
            } catch (Exception e) {
            }
        }
    }
}
