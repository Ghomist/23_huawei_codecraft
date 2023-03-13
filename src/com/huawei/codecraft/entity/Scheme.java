package com.huawei.codecraft.entity;

import com.huawei.codecraft.controller.GameController;
import com.huawei.codecraft.util.Output;
import com.huawei.codecraft.util.PriceHelper;
import com.huawei.codecraft.util.Vector2;

public class Scheme {

    public static final double AVERAGE_MAX_SPEED = 4.9;

    public CraftTable start;
    public CraftTable end;

    private GameController controller;

    private boolean isPending = false;

    private double expectTrafficTime;
    private double expectProfit;

    public Scheme(GameController controller, CraftTable start, CraftTable end) {
        this.controller = controller;
        this.start = start;
        this.end = end;
        int sellPrice = PriceHelper.getSellPrice(start.getType());
        int buyPrice = PriceHelper.getBuyPrice(start.getType());
        double distance = Vector2.distance(start.getPos(), end.getPos());
        expectTrafficTime = distance / AVERAGE_MAX_SPEED;
        double timeValueArg = PriceHelper.getTimeValueArg(expectTrafficTime);
        expectProfit = (sellPrice * timeValueArg) - buyPrice + PriceHelper.getLaterProfitByCraft(end);
    }

    public static boolean isAvailableScheme(CraftTable start, CraftTable end) {
        if (start == end || start.id == end.id)
            return false;
        int a = start.getType();
        int b = end.getType();
        if (b <= 3 || a >= 8)
            return false;
        if (b == 9)
            return true;
        switch (a) {
            case 1:
                if (b == 4 || b == 5)
                    return true;
                break;
            case 2:
                if (b == 4 || b == 6)
                    return true;
                break;
            case 3:
                if (b == 5 || b == 6)
                    return true;
                break;
            case 4:
            case 5:
            case 6:
                if (b == 7)
                    return true;
                break;
            case 7:
                if (b >= 8)
                    return true;
        }
        return false;
    }

    public double getAverageProfit(Robot robot) {
        double timeNoWait = Vector2.distance(robot.getPos(), start.getPos()) / AVERAGE_MAX_SPEED;
        double timeWait = start.getRemainFrames() / 50;
        double expectWaitTime = Math.max(timeNoWait, timeWait * 13);
        double expectTime = expectWaitTime + expectTrafficTime;
        return expectProfit / expectTime;
    }

    public void setPending() {
        isPending = true;
        start.setOrder(true);
        end.setPendingMaterial(start.getType());
    }

    public void onSending() {
        start.setOrder(false);
    }

    public void finish() {
        isPending = false;
        end.finishPendingMaterial(start.getType());
    }

    public boolean isAvailable(Robot robot) {
        return controller.getRemainTime() >= expectTrafficTime
                + Vector2.distance(robot.getPos(), start.getPos()) / AVERAGE_MAX_SPEED
                && !isPending
                && start.isProducingOrFinish()
                && !end.hasMaterial(start.getType()) && !start.isOrdered();
    }
}
