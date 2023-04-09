package com.huawei.test;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.huawei.codecraft.entity.GameMap;
import com.huawei.codecraft.entity.Robot;
import com.huawei.codecraft.entity.Workbench;
import com.huawei.codecraft.io.Input;
import com.huawei.codecraft.math.Vector2;

public class Main {

    // private static final int CNT = 20;

    public static double getTime(int a, int b) {
        return 0;
    }

    public static void main(String[] args) {

        List<Workbench> benches = new ArrayList<>();
        String line;
        int[][] grids = new int[100][100];
        int x = 0;
        int robotId = 0;
        while (Input.hasNextLine()) {
            line = Input.nextLine();
            if (line.length() < 10)
                break;
            if ("OK".equals(line)) {
                break;
            }
            for (int y = 0; y < 100; y++) {
                char c = line.charAt(y);
                switch (c) {
                    case 'A':
                        // no need for init Robots
                        // robots[robotId] = new Robot(robotId, Vector2.getPosFromGridIndex(x, y));
                        robotId++;
                        // fallthrough
                    case '.':
                        grids[x][y] = 0;
                        break;
                    case '#':
                        grids[x][y] = -1;
                        break;
                    default:
                        // boolean nearByWall = x == 0 || x == 99 || y == 0 || y == 99;
                        int id = benches.size();
                        int type = c - '0';
                        benches.add(new Workbench(id, type, Vector2.getPosFromGridIndex(x, y)));
                        grids[x][y] = c - '0';
                        break;
                }
            }
            x++;
        }
        // for (int i = 0; i < 4; ++i) {
        // robots[i] = new Robot();
        // }

        GameMap map = new GameMap(grids, benches.toArray(new Workbench[benches.size()]));
        map.findPath(null, 0,new Vector2(1, 49), new Vector2(25, 49), false);

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
