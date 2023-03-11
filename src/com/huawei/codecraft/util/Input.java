package com.huawei.codecraft.util;

import java.util.Scanner;

public class Input {
    private static final Scanner in = new Scanner(System.in);

    public static boolean hasNextLine() {
        if (in.hasNextLine())
            if (!"OK".equals(in.nextLine()))
                return true;
        return false;
    }

    public static String nextLine() {
        return in.nextLine();
    }

    public static void readUtilOK() {
        while (hasNextLine())
            ;
    }
}
