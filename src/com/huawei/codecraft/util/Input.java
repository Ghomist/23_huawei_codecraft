package com.huawei.codecraft.util;

import java.util.Scanner;

public class Input {
    private static final Scanner in = new Scanner(System.in);

    public static boolean hasNextLine() {
        return in.hasNextLine();
    }

    public static String nextLine() {
        return in.nextLine();
    }

    public static void readUntilOK() {
        while (hasNextLine()) {
            if ("OK".equals(in.nextLine())) {
                break;
            }
        }
    }
}
