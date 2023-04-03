package com.huawei.codecraft.entity;

import java.util.List;

import com.huawei.codecraft.controller.GameController;
import com.huawei.codecraft.helper.PriceHelper;
import com.huawei.codecraft.math.Vector2;

@Deprecated
public class Scheme {

    public static final double AVERAGE_MAX_SPEED = Robot.MAX_FORWARD_SPEED - 0.6;
    public static final double CHANGING_SPEED = AVERAGE_MAX_SPEED / 2;
    public static final double CHANGING_SPEED_DIST = 1;

    public Workbench start;
    public Workbench end;
    
    private int itemType;
    private GameController controller;
    private List<Vector2> path;

    private boolean isPending = false;

    private double expectTrafficTime;
    private double expectProfit;

    private boolean notRecommend;

    public Scheme(GameController controller, Workbench start, Workbench end, List<Vector2> path) {
        this.controller = controller;
        this.start = start;
        this.end = end;
        this.path = path;
        this.itemType = start.getType();
        int sellPrice = PriceHelper.getSellPrice(start.getType());
        int buyPrice = PriceHelper.getBuyPrice(start.getType());
        double distance = Vector2.distance(start.getPos(), end.getPos());
        expectTrafficTime = (distance - 3 * CHANGING_SPEED_DIST) / AVERAGE_MAX_SPEED
                + 2 * CHANGING_SPEED_DIST / CHANGING_SPEED;
        double timeValueArg = PriceHelper.getTimeValueArg(expectTrafficTime);
        expectProfit = (sellPrice * timeValueArg) - buyPrice + PriceHelper.getLaterProfitByCraft(end);
        if (controller.hasSeven) {
            notRecommend = start.getType() != 7 && end.getType() == 9;
        } else {
            notRecommend = start.getType() <= 3 && end.getType() == 9;
        }
    }

    public static boolean isAvailableScheme(Workbench start, Workbench end) {
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

    public List<Vector2> getPath() {
        return path;
    }

    public double getAverageProfit(Robot robot) {
        double distance = Vector2.distance(robot.getPos(), start.getPos());
        double timeNoWait = (distance - 2 * CHANGING_SPEED_DIST) / AVERAGE_MAX_SPEED
                + 2 * CHANGING_SPEED_DIST / CHANGING_SPEED;
        // double timeNoWait = distance / AVERAGE_MAX_SPEED;
        double timeWait = start.getRemainFrames() / 50;
        double expectWaitTime = Math.max(timeNoWait, timeWait);
        double expectTime = expectWaitTime + expectTrafficTime;
        return expectProfit / (expectTime) * controller.priority[end.getType()]
                + 750 * end.missingMaterialWeight()
                + (!controller.hasSeven && end.getType() == 9 && start.getType() >= 4 ? 300 : 0)
                - (itemType <= 3 ? 200 : 0)
                - (notRecommend ? 300 : 0);
    }

    public int getType() {
        return itemType;
    }

    public void setPending() {
        isPending = true;
        start.setOrder(true);
        end.setPendingMaterial(itemType);
    }

    public void onSending() {
        start.setOrder(false);
    }

    public void finish() {
        isPending = false;
        end.finishPendingMaterial(itemType);
    }

    public void cancelPending() {
        isPending = false;
        start.setOrder(false);
        end.finishPendingMaterial(itemType);
    }

    public boolean isAvailable(Robot robot) {
        return controller.getRemainTime() >= expectTrafficTime
                + Vector2.distance(robot.getPos(), start.getPos()) / AVERAGE_MAX_SPEED
                && !isPending
                && start.hasProduction()
                && !end.hasMaterial(itemType) && !start.isOrdered();
    }
}
