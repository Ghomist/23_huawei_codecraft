package com.huawei.codecraft.entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.huawei.codecraft.helper.LinearProgramHelper;
import com.huawei.codecraft.helper.PriceHelper;
import com.huawei.codecraft.helper.RadianHelper;
import com.huawei.codecraft.io.Output;
import com.huawei.codecraft.math.HalfPlane;
import com.huawei.codecraft.math.Line;
import com.huawei.codecraft.math.LineSegment;
import com.huawei.codecraft.math.Vector2;

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

    public static final double AVOID_DIST = 1.5; // min is 5.3 * 2 = 1.06
    public static final double AVOID_ROTATE = Math.PI / 8;
    public static final double SLOW_DOWN_RATE = 0.86;
    // public static final double AVOID_SPEED_OFFSET = 0.5;
    public static final double STOP_DIST = (AT_TABLE_DIST);

    public static final double TAO = 1; // alarm time (3 frames)
    public static final double INV_TAO = 1 / TAO;

    public int id;

    private int tableID;
    private int item;
    private float timeValueArg;
    private float impactValueArg;
    private float w;
    private float dir;
    private Vector2 v;
    private Vector2 pos;
    private Vector2 prefVelocity = new Vector2(0, 0);

    private Scheme scheme;

    private LinkedList<RobotTarget> targets = new LinkedList<>();

    private List<HalfPlane> planes = new LinkedList<>();
    private List<String> cmdList = new LinkedList<>();

    public Robot() {
        this.pos = new Vector2(0, 0);
        this.v = new Vector2(0, 0);
    }

    public void update(int id, String info) {
        this.id = id;

        String[] parts = info.split(" ");

        tableID = Integer.parseInt(parts[0]);
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

    public void schedule(Robot[] robots) {
        if (targets.size() > 0) {
            RobotTarget targetRobot = targets.getFirst();
            Vector2 targetPos = targetRobot.pos;

            double targetDir = Math.atan2(targetPos.y - pos.y, targetPos.x - pos.x);
            double prefSpeed = MAX_FORWARD_SPEED / AT_TABLE_DIST * Vector2.distance(targetPos, pos);
            prefVelocity = Vector2.getFromRadian(targetDir,
                    prefSpeed > MAX_FORWARD_SPEED ? MAX_FORWARD_SPEED : prefSpeed);

            if (isAtTable()) {
                if (targetRobot.table == null) {
                    // if (distToTarget < AT_TABLE_DIST / 2)
                    finishTarget();
                } else {
                    if (getTableID() == targetRobot.table.id) {
                        if (hasItem()) {
                            sell();
                            finishTarget();
                            scheme.finish();
                        } else if (targetRobot.table.hasProduction()) {
                            buy();
                            scheme.onSending();
                            finishTarget();
                        }
                    }
                }
            }
            avoidImpact(robots);
        } else {
            prefVelocity = new Vector2(0, 0);
        }

        // diff to pref velocity
        double diff = RadianHelper.diff(dir, prefVelocity.radian());

        // set speed to pref velocity
        double speedK = Math.cos(Math.abs(diff));
        setForwardSpeed(speedK * MAX_FORWARD_SPEED);

        // set rotate to pref velocity
        setRotateSpeed(diff * MAX_CCW_ROTATE_SPEED);
    }

    private void avoidImpact(Robot[] robots) { // RVO2
        planes.clear();

        Vector2 finalU = new Vector2(0, 0);

        for (Robot other : robots) {
            if (other.id == id || Vector2.distance(pos, other.pos) > 4)
                continue;

            final Vector2 relativePosition = other.pos.subtract(pos);
            final Vector2 relativeVelocity = getLineSpeed().subtract(other.getLineSpeed());
            final double dist2 = relativePosition.length2();
            final double rr = getRadius() + other.getRadius();
            final double rr2 = rr * rr;

            // 在三角锥的外面
            final double theta = Math.abs(Math.asin(rr / relativePosition.length()));
            final double diff = Math.acos(Vector2.cos(relativePosition, relativeVelocity));
            if (diff > theta)
                continue;

            final Vector2 direction;
            final Vector2 u;

            // 还未发生碰撞
            if (dist2 > rr2) {
                // 圆中心到相对速度
                final Vector2 w = relativeVelocity.subtract(INV_TAO, relativePosition);
                final double wLen2 = w.length2();
                final double dotProduct1 = w.dot(relativePosition);

                // 检测到要发生碰撞
                // 前端碰撞
                if (dotProduct1 < 0.0 && dotProduct1 * dotProduct1 > rr2 * wLen2) { // ?
                    final double wLength = Math.sqrt(wLen2);
                    final Vector2 unitW = w.multiply(1.0 / wLength);

                    direction = new Vector2(unitW.y, -unitW.x);
                    u = unitW.multiply(rr * INV_TAO - wLength);
                    Output.debug("forward");
                } else if (relativePosition.multiply(INV_TAO).length2()
                        - Math.pow(rr * INV_TAO, 2) < relativeVelocity.length2()) {
                    // 侧边碰撞
                    final double leg = Math.sqrt(dist2 - rr2);

                    if (Vector2.det(relativePosition, w) > 0.0) {
                        // Project on left leg. 方向向量投影到 leg
                        direction = new Vector2(
                                relativePosition.x * leg - relativePosition.y * rr,
                                relativePosition.x * rr + relativePosition.y * leg)
                                .multiply(1.0 / dist2);
                    } else {
                        // Project on right leg.
                        direction = new Vector2(
                                relativePosition.x * leg + relativePosition.y * rr,
                                -relativePosition.x * rr + relativePosition.y * leg)
                                .multiply(-1.0 / dist2);
                    }

                    final double dotProduct2 = relativeVelocity.dot(direction);
                    u = direction.multiply(dotProduct2).subtract(relativeVelocity);
                    Output.debug("side");
                } else {
                    continue;
                }
            } else {
                Output.debug("Impacted！");
                // Collision. Project on cut-off circle of time timeStep.
                final double invTimeStep = 1.0 / 0.02;

                // Vector from cutoff center to relative velocity.
                final Vector2 w = relativeVelocity.subtract(invTimeStep, relativePosition);

                final double wLength = w.length();
                final Vector2 unitW = w.multiply(1.0 / wLength);

                direction = new Vector2(unitW.y, -unitW.x);
                u = unitW.multiply(rr * invTimeStep - wLength);
            }

            final Vector2 point = getLineSpeed().add(u);
            planes.add(new HalfPlane(new Line(point, direction), u));

            finalU = finalU.add(2, u);
        }

        // final int lineFail = LinearProgramHelper.linearProgram2(lines,
        // Vector2.getFromRadian(dir, MAX_FORWARD_SPEED),
        // false);

        // if (lineFail < lines.size()) {
        // LinearProgramHelper.linearProgram3(lines, 0, lineFail);
        // }

        // prefVelocity = LinearProgramHelper.newVelocity;

        if (planes.size() != 0)
            prefVelocity = getLineSpeed().add(finalU);
    }

    private void finishTarget() {
        targets.removeFirst();
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
        targets.addLast(new RobotTarget(scheme.start));
        targets.addLast(new RobotTarget(scheme.end));
        scheme.setPending();
    }

    public LineSegment getCurrentPath() {
        return new LineSegment(pos, targets.getFirst().pos);
    }

    public void setPrefForwardSpeed(double speed) {

    }

    public void setPrefRotateSpeed(double speed) {
        cmdList.add("rotate " + id + ' ' + speed);
    }

    public void setForwardSpeed(double speed) {
        cmdList.add("forward " + id + ' ' + speed);
    }

    public void setRotateSpeed(double speed) {
        cmdList.add("rotate " + id + ' ' + speed);
    }

    public void buy() {
        cmdList.add("buy " + id);
    }

    public void sell() {
        cmdList.add("sell " + id);
    }

    public void destroy() {
        cmdList.add("destroy " + id);
    }

    public boolean isAtTable() {
        return tableID != -1;
    }

    public int getTableID() {
        return tableID;
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

    public boolean isFree() {
        return targets.size() == 0;
    }

    public List<String> getCommands() {
        return cmdList;
    }

    public double getRadius() {
        return hasItem() ? 0.53 : 0.4;
    }

    public boolean betterThan(Robot other) {
        if (this.hasItem() && !other.hasItem()) {
            return true;
        } else if (!this.hasItem() && other.hasItem()) {
            return false;
        } else if (this.hasItem() && other.hasItem()) {
            return getItem() > other.getItem();
        } else {
            return true;
        }
    }
}
