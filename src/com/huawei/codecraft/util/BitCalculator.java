package com.huawei.codecraft.util;

public class BitCalculator {
    public static boolean isOne(int num, int index) {
        return ((num >> index) & 1) == 1;
    }
}
