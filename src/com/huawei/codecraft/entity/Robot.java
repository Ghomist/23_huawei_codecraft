package com.huawei.codecraft.entity;

import java.util.LinkedList;
import java.util.List;

import com.huawei.codecraft.helper.LinearProgramHelper;
import com.huawei.codecraft.helper.MathHelper;
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

    /// RVO2 params
    public static final boolean USE_RVO2 = true; // most priority
    public static final boolean USE_RVO = false;
    public static final double RVO2_AVOID_DIST = 5; // min is 5.3 * 2 = 1.06
    public static final double RVO2_AVOID_DIST_WALL = RVO2_AVOID_DIST;
    public static final double RVO2_TAO = 1.5; // alarm time (50 frames -> 1s)
    public static final double INV_TAO = 1 / RVO2_TAO;
    public static final double RVO2_ADJUST_RATE = 1.5;
    public static final double RVO2_ADJUST_RATE_WALL = 1.35;

    public int id;

    private int tableID;
    private int item;
    private float timeValueArg;
    private float impactValueArg;
    private float w;
    private float dir;
    private Vector2 v;
    private Vector2 pos;
    private Vector2 prefVelocity = Vector2.ZERO;

    private Scheme scheme;

    private LinkedList<RobotTarget> targets = new LinkedList<>();

    private List<HalfPlane> planes = new LinkedList<>();
    private List<String> cmdList = new LinkedList<>();

    public Robot() {
        this.pos = Vector2.ZERO;
        this.v = Vector2.ZERO;
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
        v = new Vector2(vx, vy);
        dir = Float.parseFloat(parts[7]);

        float x = Float.parseFloat(parts[8]);
        float y = Float.parseFloat(parts[9]);
        pos = new Vector2(x, y);

        cmdList.clear();
    }

    public void schedule(Robot[] robots) {
        // if (id != 0)
        // return;
        if (targets.size() > 0) {
            RobotTarget targetRobot = targets.getFirst();
            Vector2 targetPos = targetRobot.pos;

            double targetDir = Math.atan2(targetPos.y - pos.y, targetPos.x - pos.x);
            double dist = Vector2.distance(targetPos, pos) * 0.8; // Todo: change param
            double prefSpeed = MathHelper.clamp(MAX_BACKWARD_SPEED, MAX_FORWARD_SPEED, dist * dist);
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
        } else {
            prefVelocity = Vector2.ZERO;
        }
        // RVO2
        avoidImpact(robots);

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

        Vector2 finalU = Vector2.ZERO;

        // detect the wall (edge)
        if (pos.y <= RVO2_AVOID_DIST_WALL) {
            final double relativeDist = -pos.y * INV_TAO;
            if (getLineSpeed().y < relativeDist) {
                Vector2 u = new Vector2(0, relativeDist - getLineSpeed().y);
                Vector2 point = getLineSpeed().add(RVO2_ADJUST_RATE_WALL, u);
                planes.add(new HalfPlane(new Line(point, Vector2.RIGHT), Vector2.UP));
                finalU = finalU.add(u);
            }
        }
        if (pos.y >= 50 - RVO2_AVOID_DIST_WALL) {
            final double relativeDist = (50 - pos.y) * INV_TAO;
            if (getLineSpeed().y > relativeDist) {
                Vector2 u = new Vector2(0, relativeDist - getLineSpeed().y);
                Vector2 point = getLineSpeed().add(RVO2_ADJUST_RATE_WALL, u);
                planes.add(new HalfPlane(new Line(point, Vector2.RIGHT), Vector2.DOWN));
                finalU = finalU.add(u);
            }
        }
        if (pos.x <= RVO2_AVOID_DIST_WALL) {
            final double relativeDist = -pos.x * INV_TAO;
            if (getLineSpeed().x < relativeDist) {
                Vector2 u = new Vector2(relativeDist - getLineSpeed().x, 0);
                Vector2 point = getLineSpeed().add(RVO2_ADJUST_RATE_WALL, u);
                planes.add(new HalfPlane(new Line(point, Vector2.UP), Vector2.RIGHT));
                finalU = finalU.add(u);
            }
        }
        if (pos.x >= 50 - RVO2_AVOID_DIST_WALL) {
            final double relativeDist = (50 - pos.x) * INV_TAO;
            if (getLineSpeed().x > relativeDist) {
                Vector2 u = new Vector2(relativeDist - getLineSpeed().x, 0);
                Vector2 point = getLineSpeed().add(RVO2_ADJUST_RATE_WALL, u);
                planes.add(new HalfPlane(new Line(point, Vector2.UP), Vector2.LEFT));
                finalU = finalU.add(u);
            }
        }

        // detect other robots
        for (Robot other : robots) {
            if (other.id == id || Vector2.distance(pos, other.pos) > RVO2_AVOID_DIST)
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
                    // Output.debug("forward");
                    final double wLength = Math.sqrt(wLen2);
                    final Vector2 unitW = w.multiply(1.0 / wLength);

                    direction = new Vector2(unitW.y, -unitW.x);
                    u = unitW.multiply(rr * INV_TAO - wLength);
                } else if (relativePosition.multiply(INV_TAO).length2()
                        - Math.pow(rr * INV_TAO, 2) < relativeVelocity.length2()) {
                    // Output.debug("side");
                    // 侧边碰撞
                    final double leg = Math.sqrt(dist2 - rr2);

                    if (Vector2.cross(relativePosition, w) > 0.0) {
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
                } else {
                    continue;
                }
            } else {
                // Output.debug("Impacted！");
                // Collision. Project on cut-off circle of time timeStep.
                final double invTimeStep = 1.0 / 0.02;

                // Vector from cutoff center to relative velocity.
                final Vector2 w = relativeVelocity.subtract(invTimeStep, relativePosition);

                final double wLength = w.length();
                final Vector2 unitW = w.multiply(1.0 / wLength);

                direction = new Vector2(unitW.y, -unitW.x);
                u = unitW.multiply(rr * invTimeStep - wLength);
            }

            final Vector2 point = getLineSpeed().add(RVO2_ADJUST_RATE, u);
            // lines.add(new HalfPlane(new Line(point, direction), u));
            planes.add(new HalfPlane(new Line(point, direction), u));

            finalU = finalU.add(RVO2_ADJUST_RATE, u);
        }

        if (USE_RVO2) {
            Vector2 p = LinearProgramHelper.shortestDistance(planes, prefVelocity);
            prefVelocity = prefVelocity.add(p);
        } else if (USE_RVO) {
            if (planes.size() != 0) {
                prefVelocity = getLineSpeed().add(finalU);
            }
        }
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
        return hasItem() ? 0.65 : 0.4;
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
