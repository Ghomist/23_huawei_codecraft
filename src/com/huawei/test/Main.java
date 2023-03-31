package com.huawei.test;

import java.util.PriorityQueue;

import com.huawei.codecraft.entity.Robot;
import com.huawei.codecraft.math.Vector2;

public class Main {

    // private static final int CNT = 20;

    public static double getTime(int a, int b) {
        return 0;
    }

    public static void main(String[] args) {
        // System.out.println("Hello Debug...");
        // System.out.println("测试：线性规划");

        // // Vector2 pos = new Vector2(0, 0);
        // List<HalfPlane> lines = new ArrayList<>();
        // lines.add(new HalfPlane(
        // new Line(new Vector2(0, 1), Vector2.RIGHT),
        // Vector2.DOWN));
        // // lines.add(new HalfPlane(
        // // new Line(new Vector2(1, 0), new Vector2(1, 1)),
        // // new Vector2(1, -1)));
        // Vector2 result = LinearProgramHelper.shortestDistance(lines,
        // Vector2.UP.multiply(2));
        // System.out.println(result.length());

        Robot a = new Robot(0, Vector2.ZERO);
        Robot b = new Robot(1, Vector2.ZERO);
        Robot c = new Robot(2, Vector2.ZERO);
        PriorityQueue<Robot> pq = new PriorityQueue<>((b_, a_) -> Integer.compare(a_.id, b_.id));
        pq.add(a);
        pq.add(b);
        pq.add(c);
        System.out.println(pq.peek().id);

        b.id = 5;

        // pq.re
        System.out.println(pq.peek().id);

    }
}
