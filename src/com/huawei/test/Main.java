package com.huawei.test;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.helper.LinearProgramHelper;
import com.huawei.codecraft.math.HalfPlane;
import com.huawei.codecraft.math.Line;
import com.huawei.codecraft.math.Vector2;

public class Main {
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
