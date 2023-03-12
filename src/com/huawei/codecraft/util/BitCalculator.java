package com.huawei.codecraft.util;

public class BitCalculator {
    public static boolean isOne(int num, int index) {
        return ((num >> index) & 1) == 1;
    }

    public static int setOne(int num, int index) {
        return (1 << index) | num;
    }

    public static int setZero(int num, int index) {
        return (~(1 << index)) & num;
    }
}
