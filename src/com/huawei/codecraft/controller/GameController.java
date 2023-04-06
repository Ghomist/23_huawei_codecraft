package com.huawei.codecraft.controller;

import java.util.LinkedList;
import java.util.List;

import com.huawei.codecraft.entity.Workbench;
import com.huawei.codecraft.entity.GameMap;
import com.huawei.codecraft.entity.Robot;
import com.huawei.codecraft.io.Input;
import com.huawei.codecraft.io.Output;
import com.huawei.codecraft.math.Vector2;

public class GameController {

    public static final int TOTAL_FRAMES_COUNT = 5 * 60 * 50;

    public static GameController instance = null;

    public boolean running;

    public int frameID;
    public int money;
    public int workbenchCountCount;
    public boolean hasSeven = false;

    public double[] priority = new double[10];
    public int sevenCount = 0;
    private GameMap map;
    private Robot[] robots = new Robot[4];
    private Workbench[] benches;

    public void init() {
        instance = this;

        final int[][] grids = new int[100][100];
        final List<Workbench> benchList = new LinkedList<>();

        String line;
        int x = 0;
        int robotId = 0;
        while (Input.hasNextLine()) {
            line = Input.nextLine();
            if ("OK".equals(line)) {
                break;
            }
            for (int y = 0; y < 100; y++) {
                char c = line.charAt(y);
                switch (c) {
                    case 'A':
                        robots[robotId] = new Robot(robotId, Vector2.getPosFromGridIndex(x, y));
                        robotId++;
                        // fallthrough
                    case '.':
                        grids[x][y] = GameMap.EMPTY;
                        break;
                    case '#':
                        grids[x][y] = GameMap.OBSTACLE;
                        break;
                    default:
                        // boolean nearByWall = x == 0 || x == 99 || y == 0 || y == 99;
                        final int id = benchList.size();
                        final int type = c - '0';
                        benchList.add(new Workbench(id, type, Vector2.getPosFromGridIndex(x, y)));
                        if (type == 7) {
                            hasSeven = true;
                            sevenCount++;
                        }
                        grids[x][y] = c - '0';
                        break;
                }
            }
            x++;
        }
        benches = benchList.toArray(new Workbench[benchList.size()]);
        map = new GameMap(grids, benches);
        start();
        running = true;
        Output.ok();
    }

    public void run() {
        while (running) {
            update();
            schedule();
            for (Robot robot : robots) {
                robot.schedule(map, robots, benches, map.getObstacles());
            }
            sendCommands();
        }
    }

    public double getRemainTime() {
        return (TOTAL_FRAMES_COUNT - frameID + 1) / 50.0;
    }

    private void start() {
        for (Robot robot : robots) {
            robot.start(map, robots, benches, map.getObstacles());
        }
        // for (int i = 0; i < benches.size(); ++i) {
        // Workbench start = benches.get(i);
        // for (int j = 0; j < benches.size(); ++j) {
        // if (i == j)
        // continue;
        // Workbench end = benches.get(j);
        // if (Scheme.isAvailableScheme(start, end)) {
        // List<Vector2> path = map.findPath(start.getPos(), end.getPos());
        // if (path != null) {
        // schemes.add(new Scheme(this, start, end, path));
        // }
        // }
        // }
        // }
        // for (Robot r : robots) {
        // if (r.id == 0) {

        // }
        // }
    }

    private void update() {
        if (!Input.hasNextLine()) {
            running = false;
            return;
        }

        String line;

        // frameID & money
        line = Input.nextLine();
        String[] lineSplit = line.split(" ");
        frameID = Integer.parseInt(lineSplit[0]);
        money = Integer.parseInt(lineSplit[1]);

        // craft bench count
        line = Input.nextLine();
        workbenchCountCount = Integer.parseInt(line);

        // craft bench update
        for (int i = 0; i < workbenchCountCount; ++i) {
            line = Input.nextLine();
            benches[i].update(line);
        }

        // robot update
        for (int i = 0; i < 4; ++i) {
            line = Input.nextLine();
            robots[i].update(i, line);
        }

        Input.readUntilOK();
    }

    private void schedule() {
    }

    private void sendCommands() {
        Output.send(frameID);
        for (Robot robot : robots) {
            for (String cmd : robot.getCommands())
                Output.send(cmd);
        }
        Output.ok();
    }

}
