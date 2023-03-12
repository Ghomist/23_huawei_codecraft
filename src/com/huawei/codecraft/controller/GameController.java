package com.huawei.codecraft.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.huawei.codecraft.entity.CraftTable;
import com.huawei.codecraft.entity.GameMap;
import com.huawei.codecraft.entity.Robot;
import com.huawei.codecraft.util.Input;
import com.huawei.codecraft.util.Output;
import com.huawei.codecraft.util.Vector2;

public class GameController {

    private boolean running;

    private int frameID;
    private int money;
    private int craftTableCount;

    private GameMap map;
    private Robot[] robots = new Robot[4];
    private List<CraftTable> tables = new ArrayList<>();

    public void init() {
        map = new GameMap();
        String line;
        int x = 0;
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
                        // robots.add(new Robot(Vector2.getPosFromGridIndex(x, y)));
                        // fallthrough
                    case '.':
                        map.SetGridType(x, y, 0);
                        break;
                    default:
                        tables.add(new CraftTable(tables.size()));
                        map.SetGridType(x, y, c - '0');
                        break;
                }
            }
            x++;
        }
        for (int i = 0; i < 4; ++i) {
            robots[i] = new Robot();
        }
        start();
        running = true;
        Output.ok();
    }

    public void run() {
        while (running) {
            update();
            schedule();
            for (Robot robot : robots) {
                robot.schedule();
            }
            sendCommands();
        }
    }

    private void start() {
        // Todo: init schedule
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

        // craft table count
        line = Input.nextLine();
        craftTableCount = Integer.parseInt(line);

        // craft table update
        for (int i = 0; i < craftTableCount; ++i) {
            line = Input.nextLine();
            tables.get(i).update(line);
        }

        // robot update
        for (int i = 0; i < 4; ++i) {
            line = Input.nextLine();
            robots[i].update(i, line);
        }

        Input.readUntilOK();
    }

    private void schedule() {
        for (Robot robot : robots) {
            if (!robot.hasTarget()) {
                int target = new Random().nextInt(craftTableCount);
                robot.setTargetTable(tables.get(target));
                Output.debug(robot.id + " -> " + target);
            }
        }
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
