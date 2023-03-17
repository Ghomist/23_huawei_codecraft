package com.huawei.test;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.helper.LinearProgramHelper;
import com.huawei.codecraft.math.HalfPlane;
import com.huawei.codecraft.math.Line;
import com.huawei.codecraft.math.Vector2;

public class Main {

    private static final int CNT = 20;

    public static double getTime(int a, int b) {
        return 0;
    }

    public static double getMax(int r1, int r2, int r3, int r4, double t) {
        double tMin1 = Double.MAX_VALUE;
        int minTable1 = 0;
        for (int i = 0; i < CNT; i++) {
            if (i == r1)
                continue;
            double t1 = getTime(r1, i);
            if (t1 < tMin1) {
                tMin1 = t1;
                minTable1 = i;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println("Hello Debug...");
        System.out.println("测试：线性规划");

        Vector2 pos = new Vector2(0, 0);
        List<HalfPlane> lines = new ArrayList<>();
        lines.add(new HalfPlane(
                new Line(new Vector2(1, 0), new Vector2(1, -1)),
                new Vector2(1, 1)));
        lines.add(new HalfPlane(
                new Line(new Vector2(1, 0), new Vector2(1, 1)),
                new Vector2(1, -1)));
        Vector2 result = LinearProgramHelper.shortestDistance(lines, pos);
        System.out.println(result.length());
    }
}
