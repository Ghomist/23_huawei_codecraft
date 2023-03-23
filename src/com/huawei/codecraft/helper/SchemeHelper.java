package com.huawei.codecraft.helper;

import com.huawei.codecraft.controller.GameController;
import com.huawei.codecraft.entity.Robot;
import com.huawei.codecraft.entity.Workbench;
import com.huawei.codecraft.math.Vector2;

public class SchemeHelper {

    public static final double AVERAGE_MAX_SPEED = Robot.MAX_FORWARD_SPEED - 0.6;
    public static final double CHANGING_SPEED = AVERAGE_MAX_SPEED / 2;
    public static final double CHANGING_SPEED_DIST = 1;

    public static double getExpectTrafficTime(Workbench a, Workbench b) {
        double distance = Vector2.distance(a.getPos(), b.getPos());
        return (distance - 3 * CHANGING_SPEED_DIST) / AVERAGE_MAX_SPEED + 2 * CHANGING_SPEED_DIST / CHANGING_SPEED;
    }

    public static double getExpectProfit(Workbench start, Workbench end) {
        double expectTrafficTime = getExpectTrafficTime(start, end);
        double timeValueArg = PriceHelper.getTimeValueArg(expectTrafficTime);
        int sellPrice = PriceHelper.getSellPrice(start.getType());
        int buyPrice = PriceHelper.getBuyPrice(start.getType());
        return (sellPrice * timeValueArg) - buyPrice + PriceHelper.getLaterProfitByCraft(end);
    }

    public static double getArriveTime(Workbench bench, Robot robot) {
        double distance = Vector2.distance(robot.getPos(), bench.getPos());
        return (distance - 2 * CHANGING_SPEED_DIST) / AVERAGE_MAX_SPEED + 2 * CHANGING_SPEED_DIST / CHANGING_SPEED;
        // return distance / AVERAGE_MAX_SPEED;
    }

    public static double getExpectWaitTime(Workbench bench, Robot robot) {
        double timeNoWait = getArriveTime(bench, robot);
        double timeWait = bench.getRemainFrames() / 50;
        return Math.max(timeNoWait, timeWait * 13);
    }

    public static boolean needWait(Workbench bench, Robot robot) {
        return getArriveTime(bench, robot) <= bench.getRemainFrames() / 50;
    }

    public static double getAverageProfit(Workbench start, Workbench end, Robot robot) {
        double expectTime = getExpectWaitTime(start, robot) + getExpectTrafficTime(start, end);
        return getExpectProfit(start, end) / expectTime;
    }

    public static boolean isAvailable(Workbench bench, Robot robot) {
        return bench.isProducingOrFinish()
                && !bench.isOrdered()
                && !needWait(bench, robot);
        // GameController.instance.getRemainTime() >= expectTrafficTime
        // + Vector2.distance(robot.getPos(), bench.getPos()) / AVERAGE_MAX_SPEED
        // && !isPending

        // && !end.hasMaterial(itemType)
    }
}
