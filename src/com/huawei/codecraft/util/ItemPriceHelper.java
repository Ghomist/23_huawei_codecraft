package com.huawei.codecraft.util;

public class ItemPriceHelper {
    public static int getBuyPrice(int item) {
        switch (item) {
            case 1:
                return 3000;
            case 2:
                return 4400;
            case 3:
                return 5800;
            case 4:
                return 15400;
            case 5:
                return 17200;
            case 6:
                return 19200;
            case 7:
                return 76000;
            default:
                return 0;
        }
    }

    public static int getSalePrice(int item) {
        switch (item) {
            case 1:
                return 6000;
            case 2:
                return 7600;
            case 3:
                return 9200;
            case 4:
                return 22500;
            case 5:
                return 25000;
            case 6:
                return 27500;
            case 7:
                return 105000;
            default:
                return 0;
        }
    }
}
