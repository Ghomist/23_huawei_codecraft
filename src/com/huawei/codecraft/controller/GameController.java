package com.huawei.codecraft.controller;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.entity.Workbench;
import com.huawei.codecraft.entity.GameMap;
import com.huawei.codecraft.entity.Robot;
import com.huawei.codecraft.io.Input;
import com.huawei.codecraft.io.Output;
import com.huawei.codecraft.math.Vector2;

public class GameController {

    public static final int TOTAL_FRAMES_COUNT = 3 * 60 * 50;

    public static GameController instance = null;

    public boolean running;

    public int frameID;
    public int money;
    public int workbenchCountCount;
    public boolean hasSeven = false;

    private GameMap map;
    private Robot[] robots = new Robot[4];
    private List<Workbench> benches = new ArrayList<>();

    public void init() {
        instance = this;
        map = new GameMap();
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
                        // no need for init Robots
                        robots[robotId] = new Robot(robotId, Vector2.getPosFromGridIndex(x, y));
                        robotId++;
                        // fallthrough
                    case '.':
                        map.SetGridType(x, y, 0);
                        break;
                    default:
                        // boolean nearByWall = x == 0 || x == 99 || y == 0 || y == 99;
                        int id = benches.size();
                        int type = c - '0';
                        benches.add(new Workbench(id, type, Vector2.getPosFromGridIndex(x, y)));
                        if (type == 7)
                            hasSeven = true;
                        map.SetGridType(x, y, c - '0');
                        break;
                }
            }
            x++;
        }
        // for (int i = 0; i < 4; ++i) {
        // robots[i] = new Robot();
        // }
        start();
        running = true;
        Output.ok();
    }

    public void run() {
        while (running) {
            update();
            schedule();
            for (Robot robot : robots) {
                robot.schedule(robots);
                // robot.avoidImpact(robots);
            }
            sendCommands();
        }
    }

    public double getRemainTime() {
        return (TOTAL_FRAMES_COUNT - frameID + 1) * 20 / 1000;
    }

    private void start() {
        switch (benches.size()) {
            case 43:
                robots[0].addTargets(Scheduler.path_43[0], benches);
                robots[1].addTargets(Scheduler.path_43[1], benches);
                robots[2].addTargets(Scheduler.path_43[2], benches);
                robots[3].addTargets(Scheduler.path_43[3], benches);
                break;
        }
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
            benches.get(i).update(line);
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
