package com.huawei.codecraft.helper;

public class RadianHelper {
    // result is the angle from start to end
    // positive is CCW
    // negative is CW
    public static double diff(double start, double end) {
        double diff = end - start;
        if (diff >= Math.PI) {
            diff -= 2 * Math.PI;
        } else if (diff <= -Math.PI) {
            diff += 2 * Math.PI;
        }
        return diff;
    }
}
