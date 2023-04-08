package com.huawei.codecraft.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.huawei.codecraft.controller.GameController;
import com.huawei.codecraft.helper.LinearProgramHelper;
import com.huawei.codecraft.helper.PriceHelper;
import com.huawei.codecraft.helper.RadianHelper;
import com.huawei.codecraft.io.Output;
import com.huawei.codecraft.math.HalfPlane;
import com.huawei.codecraft.math.Line;
import com.huawei.codecraft.math.LineSegment;
import com.huawei.codecraft.math.Vector2;
import com.huawei.codecraft.util.BitCalculator;

public class Robot {

    public static final double MAX_FORWARD_SPEED = 6.0;
    public static final double MAX_BACKWARD_SPEED = -2.0;
    public static final double MAX_CW_ROTATE_SPEED = -Math.PI;
    public static final double MAX_CCW_ROTATE_SPEED = Math.PI;
    public static final double DENSITY = 20;
    public static final double RADIUS = 0.45;
    public static final double RADIUS_CARRY = 0.53;
    public static final double MASS = Math.PI * RADIUS * RADIUS * DENSITY;
    public static final double BENCH_TEST_DIST = 0.4;

    // Dist-to-Stop params
    public static final double DIST_ARRIVE = 0.35; // go to next target
    public static final double DIST_SLOW_DOWN = 1.0; // set speed down
    public static final double DIST_STOP = 0.35; // set speed to 0

    // RVO2 params
    public static final boolean USE_RVO2 = true; // most priority
    public static final boolean USE_RVO = false;
    public static final double RVO2_AVOID_DIST = 5; // min is 5.3 * 2 = 1.06
    public static final double RVO2_AVOID_DIST_WALL = RVO2_AVOID_DIST - 2;
    public static final double RVO2_TAO = 1.5; // alarm time (50 frames -> 1s)
    public static final double INV_TAO = 1 / RVO2_TAO;
    public static final double RVO2_ADJUST_RATE_HIGH = 1.6;
    public static final double RVO2_ADJUST_RATE_LOW = 1.3;
    public static final double RVO2_ADJUST_RATE_WALL = 1.45;
    public static final boolean RVO2_AVOID_WALL = true;
    public static final boolean RVO2_AVOID_EDGE = true;

    public int id;

    private int atWorkbenchID;
    private int item;
    private double timeValueArg;
    private double impactValueArg;
    private double w;
    private double dir;
    private Vector2 v;
    private Vector2 pos;
    private Vector2 prefVelocity = Vector2.ZERO;

    private Scheme myScheme = null;
    private final LinkedList<Vector2> path = new LinkedList<>();
    private Vector2 lastTarget;

    private List<HalfPlane> planes = new LinkedList<>();
    private List<String> cmdList = new LinkedList<>();

    public Robot() {
        this.pos = Vector2.ZERO;
        this.v = Vector2.ZERO;
    }

    public Robot(int id, Vector2 pos) {
        this.id = id;
        this.pos = pos;
        this.v = Vector2.ZERO;
    }

    public void update(int id, String info) {
        this.id = id;

        String[] parts = info.split(" ");

        atWorkbenchID = Integer.parseInt(parts[0]);
        item = parts[1].charAt(0) - '0';
        timeValueArg = Double.parseDouble(parts[2]);
        impactValueArg = Double.parseDouble(parts[3]);
        w = Double.parseDouble(parts[4]);
        final double vx = Double.parseDouble(parts[5]);
        final double vy = Double.parseDouble(parts[6]);
        v = new Vector2(vx, vy);
        dir = Double.parseDouble(parts[7]);

        final double x = Double.parseDouble(parts[8]);
        final double y = Double.parseDouble(parts[9]);
        pos = new Vector2(x, y);

        cmdList.clear();
    }

    public void schedule(GameMap map, Robot[] robots, Workbench[] benches, Vector2[] obstacles) {
        // self schedule
        selfSchedule(map, benches);

        // test stuck
        avoidStuck(robots);

        // generate pref velocity according to target
        genPrefVelocity();

        // RVO2
        avoidImpact(robots, obstacles);

        // calculate diff to pref velocity
        final double diff = RadianHelper.diff(dir, prefVelocity.radian());

        // lead current velocity to pref velocity
        final double speedK = Math.cos(Math.abs(diff));
        if (Math.abs(diff) < Math.PI / 7) {
            setForwardSpeed(speedK * prefVelocity.length());
        } else {
            setForwardSpeed(speedK * prefVelocity.length() / 5);
            // setForwardSpeed(0);
        }

        // rotate to pref velocity
        setRotateSpeed(diff * MAX_CCW_ROTATE_SPEED);
    }

    private void finishTarget() {
        Vector2 last = path.removeFirst();
        lastTarget = last;
    }

    public void resetTargets(List<Vector2> list) {
        this.path.clear();
        this.path.addAll(list);
    }

    public void addTargets(List<Vector2> list) {
        this.path.addAll(list);
    }

    public void insertTarget(Vector2 pos) {
        if (pos != null && pos != lastTarget)
            path.addFirst(pos);
    }

    public void start(GameMap map, Robot[] robots, Workbench[] benches, Vector2[] obstacles) {
        selfSchedule(map, benches);
    }

    private Vector2[] posWindow = new Vector2[30];
    private int posIndex = 0;
    private int coolDownFrame = 50;

    private void avoidStuck(Robot[] robots) {
        Vector2 prePos = posWindow[posIndex];
        posWindow[posIndex] = pos;
        posIndex++;
        if (posIndex >= posWindow.length) {
            posIndex -= posWindow.length;
        }
        if (coolDownFrame > 0) {
            coolDownFrame--;
        } else {
            if (prePos != null && Vector2.distance(prePos, pos) < 0.001 && path.size() != 0) {
                // Output.debug(id + " is stuck " + GameController.frameID);
                boolean nearRobot = false;
                for (Robot r : robots) {
                    if (r == this)
                        continue;
                    if (Vector2.distance(pos, r.pos) < 1.1) {
                        nearRobot = true;
                        break;
                    }
                }
                if (nearRobot) {
                    insertTarget(lastTarget);
                } else {
                    Vector2 line = path.getFirst().subtract(lastTarget);
                    Vector2 newTarget = new Line(lastTarget, line).getProjection(pos); // TODO: no offset
                    insertTarget(newTarget);
                }
                coolDownFrame = 20;
            }
        }
    }

    private void selfSchedule(GameMap map, Workbench[] benches) {
        // TODO: 决策
        if (hasItem()) {
            // sell item
            if (isAtWorkbench() && myScheme != null) {
                if (getAtWorkbenchID() == myScheme.sell.id) {
                    sell();
                    myScheme.finishPending();
                    myScheme = null;
                    return;
                }
            }
            if (myScheme == null) {
                if (this.path.size() == 0) {
                    int targetID = map.getClosestWorkbench(pos, BitCalculator.setOne(0, getItem()), true);
                    List<Vector2> path = map.findPath(pos, benches[targetID].getPos(), true);
                    resetTargets(path);
                }
                sell();
            }

        } else {
            // buy
            if (isAtWorkbench() && myScheme != null) {
                if (getAtWorkbenchID() == myScheme.buy.id) {
                    buy();

                    // head to sell
                    // TODO: 这里居然还有寻不到终点去的
                    List<Vector2> path = map.findPath(pos, myScheme.sell.getPos(), true);
                    if (path != null) {
                        resetTargets(path);
                    }

                    myScheme.startSending();

                    // skip frame
                    return;
                }
            }

            // get all
            List<Scheme> schemes = map.getSchemes();

            // filter to available and find best
            // TODO: 策略需要优化
            // Scheme newScheme = schemes.stream()
            // .filter(x -> x.canPending() && map.getDistToWorkbench(pos, x.buy.id, false)
            // != Double.MAX_VALUE)
            // .min(Comparator.comparing(s -> map.getDistToWorkbench(pos, s.buy.id, false) +
            // s.getDistBetween()))
            // .orElse(null);
            Scheme newScheme = null;
            double cost = 0;
            for (Scheme s : schemes) {
                double d = map.getDistToWorkbench(pos, s.buy.id, false);
                // if (d == Double.MAX_VALUE)
                //     System.err.println(GameController.frameID + " " + id + " " + d + " " + s.buy.id);
                if (s.canPending() && d != Double.MAX_VALUE) {
                    double c = map.getDistToWorkbench(pos, s.buy.id, false) + s.getDistBetween();
                    if (newScheme == null || c < cost) {
                        newScheme = s;
                        cost = c;
                    }
                }
            }

            if (newScheme == null)
                return;

            // test if need to change
            boolean needToChange = false;
            if (myScheme == null) {
                needToChange = true;
            } else {
                double dist = map.getDistToWorkbench(pos, myScheme.buy.id, false);
                double newDist = map.getDistToWorkbench(pos, newScheme.buy.id, false);
                needToChange = newDist < dist;
            }

            // set new scheme
            if (needToChange) {
                // cancel last one
                if (myScheme != null) {
                    myScheme.cancelPending();
                    myScheme = null;
                }

                // re-find path
                List<Vector2> path = map.findPath(pos, newScheme.buy.getPos(), false);
                if (path == null)
                    return;

                // set new
                myScheme = newScheme;
                myScheme.setPending();
                resetTargets(path);
            }
        }
    }

    private void genPrefVelocity() {
        if (path.size() > 0) {
            // get target
            final Vector2 targetPos = path.getFirst();

            // find target direction
            // Output.debug(targetPos);
            // Output.debug(pos);
            final double targetDir = Math.atan2(targetPos.y - pos.y, targetPos.x - pos.x);

            // find best speed
            final double distToTarget = Vector2.distance(targetPos, pos);
            double prefSpeed;
            if (distToTarget < DIST_STOP) {
                // stop
                prefSpeed = 0;
            } else if (distToTarget < DIST_SLOW_DOWN) {
                // slow down
                prefSpeed = MAX_FORWARD_SPEED * (distToTarget / DIST_SLOW_DOWN);
            } else {
                // full speed forward
                prefSpeed = MAX_FORWARD_SPEED;
            }

            // calculate best velocity
            prefVelocity = Vector2.getFromRadian(targetDir, prefSpeed);

            // if arrive target
            if (distToTarget < DIST_ARRIVE) {
                finishTarget();
            }
        } else {
            // no target, stop and wait
            prefVelocity = Vector2.ZERO;
        }
    }

    private void avoidImpact(Robot[] robots, Vector2[] obstacles) { // RVO2
        planes.clear();

        Vector2 finalU = Vector2.ZERO;

        // detect the wall (edge)
        if (RVO2_AVOID_EDGE) {
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
        }

        // detect obstacles (recognize wall as circle)
        if (RVO2_AVOID_WALL)
            for (Vector2 obsPos : obstacles) {
                if (Vector2.distance(pos, obsPos) > 2.5)
                    continue;

                final Vector2 relativePosition = obsPos.subtract(pos);
                final Vector2 relativeVelocity = getLineSpeed();
                final double dist2 = relativePosition.length2();
                final double rr = getRadius() + 0.25;
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

                final Vector2 point = getLineSpeed().add(0.6, u);
                // lines.add(new HalfPlane(new Line(point, direction), u));
                // planes.add(new HalfPlane(new Line(point, direction), u));

                finalU = finalU.add(1.5, u);
            }
        prefVelocity = prefVelocity.add(finalU);
        finalU = Vector2.ZERO;

        // detect other robots
        for (Robot other : robots) {
            if (other.id == id || Vector2.distance(pos, other.pos) > RVO2_AVOID_DIST)
                continue;

            if (Math.abs(RadianHelper.diff(dir, other.dir)) > Math.PI * 3 / 4)
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

            final Vector2 point = getLineSpeed().add(getAdjustRate(), u);
            // lines.add(new HalfPlane(new Line(point, direction), u));
            planes.add(new HalfPlane(new Line(point, direction), u));

            finalU = finalU.add(getAdjustRate(), u);
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

    public LineSegment getCurrentPath() {
        return new LineSegment(pos, path.getFirst());
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

    public boolean isAtWorkbench() {
        return atWorkbenchID != -1;
    }

    public int getAtWorkbenchID() {
        return atWorkbenchID;
    }

    public boolean hasItem() {
        return item != 0;
    }

    public int getItem() {
        return item;
    }

    public double getItemPrice() {
        return PriceHelper.getSellPrice(item) * timeValueArg * impactValueArg;
    }

    public double getAngleSpeed() {
        return w;
    }

    public Vector2 getLineSpeed() {
        return v;
    }

    public Vector2 getPos() {
        return pos;
    }

    public double getDir() {
        return dir;
    }

    public boolean isFree() {
        return path.size() == 0;
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

    private double getAdjustRate() {
        return hasItem() ? RVO2_ADJUST_RATE_LOW : RVO2_ADJUST_RATE_HIGH;
    }
}
