package com.huawei.codecraft.entity;

import java.util.LinkedList;
import java.util.List;

import com.huawei.codecraft.util.PriceHelper;
import com.huawei.codecraft.util.Output;
import com.huawei.codecraft.util.Vector2;

public class Robot {

    public static final double MAX_FORWARD_SPEED = 6.0;
    public static final double MAX_BACKWARD_SPEED = -2.0;
    public static final double MAX_CW_ROTATE_SPEED = -Math.PI;
    public static final double MAX_CCW_ROTATE_SPEED = Math.PI;
    public static final double DENSITY = 20;
    public static final double RADIUS = 0.45;
    public static final double RADIUS_CARRY = 0.53;
    public static final double MASS = Math.PI * RADIUS * RADIUS * DENSITY;
    public static final double AT_TABLE_DIST = 0.4;

    public static final double AVOID_DIST = 1.09; // min is 5.3 * 2 = 1.06
    public static final double SLOW_DOWN_RATE = 0.86;
    // public static final double AVOID_SPEED_OFFSET = 0.5;
    public static final double STOP_DIST = AT_TABLE_DIST + 0.00098;

    public int id;

    private int tableType;
    private int item;
    private float timeValueArg;
    private float impactValueArg;
    private float w;
    private float dir;
    private Vector2 v;
    private Vector2 pos;

    private boolean isWaitingProduce = false;
    private Scheme scheme = null;
    private Vector2 targetPos = null;
    private int targetTableID = -1;

    private boolean avoidImpact = false;
    private Robot impactRobot = null;

    private List<String> cmdList = new LinkedList<>();

    public Robot() {
        this.pos = new Vector2(0, 0);
        this.v = new Vector2(0, 0);
    }

    public void update(int id, String info) {
        this.id = id;

        String[] parts = info.split(" ");

        tableType = Integer.parseInt(parts[0]);
        item = parts[1].charAt(0) - '0';
        timeValueArg = Float.parseFloat(parts[2]);
        impactValueArg = Float.parseFloat(parts[3]);
        w = Float.parseFloat(parts[4]);
        float vx = Float.parseFloat(parts[5]);
        float vy = Float.parseFloat(parts[6]);
        v.set(vx, vy);
        dir = Float.parseFloat(parts[7]);

        float x = Float.parseFloat(parts[8]);
        float y = Float.parseFloat(parts[9]);
        pos.set(x, y);

        cmdList.clear();
    }

    public void schedule() {
        if (scheme != null) {
            if (!hasItem()) {
                setTargetTable(scheme.start);
            } else {
                setTargetTable(scheme.end);
            }
        }
        if (isWaitingProduce) {
            if (getTableID() == targetTableID) {
                if (scheme != null && scheme.start.hasProduction()) {
                    buy();
                    scheme.onSending();
                    isWaitingProduce = false;
                }
            }
        }
        if (targetPos != null) {
            double targetDir;
            if (!avoidImpact) {
                targetDir = Math.atan2(targetPos.y - pos.y, targetPos.x - pos.x);
            } else {
                Vector2 avoidPos = impactRobot.getPos();
                targetDir = Math.atan2(pos.y - avoidPos.y, pos.x - avoidPos.x);
            }

            double diff = targetDir - dir;
            if (diff >= Math.PI) {
                diff = 2 * Math.PI - diff;
            } else if (diff <= -Math.PI) {
                diff += 2 * Math.PI;
            }

            double speedK = Math.cos(Math.abs(diff));
            double speed = speedK >= 0 ? MAX_FORWARD_SPEED : MAX_BACKWARD_SPEED - 2.10011;
            if (avoidImpact)
                speed *= SLOW_DOWN_RATE;
            setRotateSpeed(MAX_CCW_ROTATE_SPEED * diff);

            double dist = Vector2.distance(targetPos, pos);
            boolean stop = dist < STOP_DIST;

            if (stop) {
                setForwardSpeed(-speedK * speed);
                if (getTableID() == targetTableID) {
                    if (targetTableID == scheme.start.id) {
                        isWaitingProduce = true;
                    } else {
                        sell();
                        scheme.finish();
                        scheme = null;
                    }
                    targetPos = null;
                }
            } else {
                setForwardSpeed(speed * speedK);
            }
        } else {
            setForwardSpeed(0);
        }
    }

    public void avoidImpact(Robot impactRobot) {
        if (impactRobot != null) {
            avoidImpact = true;
            this.impactRobot = impactRobot;
        } else {
            avoidImpact = false;
            this.impactRobot = null;
        }
    }

    public void setTargetScheme(Scheme scheme) {
        this.scheme = scheme;
        scheme.setPending();
    }

    public void setTargetTable(CraftTable table) {
        targetPos = table.getPos();
        targetTableID = table.id;
    }

    public void setForwardSpeed(double speed) {
        cmdList.add("forward " + id + ' ' + speed);
    }

    public void setRotateSpeed(double speed) {
        cmdList.add("rotate " + id + ' ' + speed);
    }

    public void buy() {
        if (!hasItem())
            cmdList.add("buy " + id);
    }

    public void sell() {
        cmdList.add("sell " + id);
    }

    public void destroy() {
        cmdList.add("destroy " + id);
    }

    public boolean isAtTable() {
        return tableType != -1;
    }

    public int getTableID() {
        return tableType;
    }

    public boolean hasItem() {
        return item != 0;
    }

    public int getItem() {
        return item;
    }

    public float getItemPrice() {
        return PriceHelper.getSellPrice(item) * timeValueArg * impactValueArg;
    }

    public float getAngleSpeed() {
        return w;
    }

    public Vector2 getLineSpeed() {
        return v;
    }

    public Vector2 getPos() {
        return pos;
    }

    public float getDir() {
        return dir;
    }

    public boolean hasTarget() {
        return targetPos != null;
    }

    public boolean isFree() {
        return scheme == null;
    }

    public List<String> getCommands() {
        return cmdList;
    }

    public double getRadius() {
        return hasItem() ? 0.53 : 0.4;
    }
}
