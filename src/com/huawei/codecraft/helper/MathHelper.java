package com.huawei.codecraft.helper;

public class MathHelper {
    public static double clamp(double start, double end, double num) {
        if (num < start) {
            return start;
        } else if (num > end) {
            return end;
        } else {
            return num;
        }
    }

    public static int round(double n) {
        int integer = (int) n;
        double decimal = n - integer;
        return decimal > 0.5 ? integer + 1 : integer;
    }
}
