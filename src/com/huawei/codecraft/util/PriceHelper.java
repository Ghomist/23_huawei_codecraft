package com.huawei.codecraft.util;

import com.huawei.codecraft.entity.CraftTable;

public class PriceHelper {
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

    public static int getSellPrice(int item) {
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

    public static int getLaterProfitByCraft(CraftTable table) {
        int type = table.getType();
        if (type <= 3 || type >= 8)
            return 0;
        int profit = getSellPrice(type) - getBuyPrice(type);
        return profit / (type == 7 ? 60 : 20);
        // return 0;
    }

    public static double getTimeValueArg(double holdTime) {
        double holdFrames = holdTime * 50;
        return f(holdFrames, 9000, 0.8);
    }

    private static double f(double x, double maxX, double minRate) {
        if (x < maxX) {
            double k = 1 - x / maxX;
            k *= k;
            return (1 - Math.sqrt(1 - k)) * (1 - minRate) + minRate;
        } else {
            return minRate;
        }
    }
}
