package com.huawei.codecraft.controller;

import java.util.ArrayList;
import java.util.List;

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
    private List<Robot> robots = new ArrayList<>();
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
                        robots.add(new Robot(Vector2.GetPosFromGridIndex(x, y)));
                        // fallthrough
                    case '.':
                        map.SetGridType(x, y, 0);
                        break;
                    default:
                        tables.add(new CraftTable(Vector2.GetPosFromGridIndex(x, y)));
                        map.SetGridType(x, y, c - '0');
                        break;
                }

            }
            x++;
        }
        start();
        running = true;
        Output.ok();
    }

    public void run() {
        while (running) {
            update();
            schedule();
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
            robots.get(i).update(i, line);
        }

        Input.readUntilOK();
    }

    private void schedule() {
        // Todo something
        robots.get(0).setRotateSpeed((float) Math.PI);
        Output.debug(robots.get(0).getDir());
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
