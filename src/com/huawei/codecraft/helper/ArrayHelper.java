package com.huawei.codecraft.helper;

public class ArrayHelper {
    public static int safeGet(int[][] arr, int x, int y, int err) {
        if (x < 0 || x >= arr.length || y < 0 || y >= arr[0].length)
            return err;
        return arr[x][y];
    }

    public static void safeSet(int[][] arr, int x, int y, int value) {
        if (x < 0 || x >= arr.length || y < 0 || y >= arr[0].length)
            return;
        arr[x][y] = value;
    }

    public static <T> T safeGet(T[][] arr, int x, int y, T err) {
        if (x < 0 || x >= arr.length || y < 0 || y >= arr[0].length)
            return err;
        return arr[x][y];
    }

    public static <T> void safeSet(T[][] arr, int x, int y, T value) {
        if (x < 0 || x >= arr.length || y < 0 || y >= arr[0].length)
            return;
        arr[x][y] = value;
    }
}
