package com.huawei.codecraft.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.huawei.codecraft.entity.Workbench;
import com.huawei.codecraft.entity.GameMap;
import com.huawei.codecraft.entity.Request;
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

    private GameMap map;
    public Robot[] robots = new Robot[4];
    public List<Workbench> benches = new ArrayList<>();
    public List<Request> requests = new ArrayList<>();

    private void start() {
        for (Workbench workbench : benches) {
            workbench.publishRequest();
        }
    }

    private void schedule() {
        Iterator<Request> it = requests.iterator();
        while (it.hasNext()) {
            Request request = it.next();
            if (request.isFinished()) {
                if (!request.tryReboot()) {
                    it.remove();
                }
            }
        }
    }

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
                        Workbench bench = new Workbench(id, type, Vector2.getPosFromGridIndex(x, y));
                        benches.add(bench);
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
            }
            sendCommands();
        }
    }

    public double getRemainTime() {
        return (TOTAL_FRAMES_COUNT - frameID + 1) * 20 / 1000;
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

    private void sendCommands() {
        Output.send(frameID);
        for (Robot robot : robots) {
            for (String cmd : robot.getCommands())
                Output.send(cmd);
        }
        Output.ok();
    }

    public void publishRequest(Request request) {
        requests.add(request);
    }
}
