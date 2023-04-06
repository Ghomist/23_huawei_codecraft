package com.huawei.codecraft.entity;

public class Scheme {

    public final Workbench buy;
    public final Workbench sell;
    public final int itemType;

    // private final GameMap map;
    private final double distBetween;

    private boolean isPending = false;

    public Scheme(GameMap map, Workbench buy, Workbench sell) {
        this.buy = buy;
        this.sell = sell;
        this.itemType = buy.getType();
        // this.map = map;
        distBetween = map.getDistToWorkbench(buy.getPos(), sell.id, true);
    }

    public double getDistBetween() {
        return distBetween;
    }

    public void setPending() {
        isPending = true;
        buy.order();
        sell.setPendingMaterial(itemType);
    }

    public void cancelPending() {
        isPending = false;
        buy.cancelOrder();
        sell.clearPendingMaterial(itemType);
    }

    public void startSending() {
        buy.cancelOrder();
    }

    public void finishPending() {
        isPending = false;
        sell.clearPendingMaterial(itemType);
    }

    /**
     * 考虑当前状况是否可以进行这个 Scheme
     */
    public boolean canPending() {
        return !isPending
                && !buy.isOrdered()
                && buy.hasProduction()
                // && buy.isProducingOrFinish()
                && !sell.hasMaterial(itemType);
    }

    /**
     * 检查是否可以交易，考虑了路径不通以及工作台种类
     * 
     * @param map   GameMap 实例
     * @param start 买的工作台
     * @param end   卖的工作台
     * @return 是否可以交易
     */
    public static boolean isAvailableScheme(GameMap map, Workbench start, Workbench end) {
        if (start == end || start.id == end.id)
            return false;
        int a = start.getType();
        int b = end.getType();
        if (b <= 3 || a >= 8)
            return false;

        boolean canTrade = false;
        if (b == 9)
            canTrade = true;
        else
            switch (a) {
                case 1:
                    if (b == 4 || b == 5)
                        canTrade = true;
                    break;
                case 2:
                    if (b == 4 || b == 6)
                        canTrade = true;
                    break;
                case 3:
                    if (b == 5 || b == 6)
                        canTrade = true;
                    break;
                case 4:
                case 5:
                case 6:
                    if (b == 7)
                        canTrade = true;
                    break;
                case 7:
                    if (b >= 8)
                        canTrade = true;
            }

        if (!canTrade)
            return false;
        double dist = map.getDistToWorkbench(start.getPos(), end.id, true);
        return dist != Double.MAX_VALUE;
    }
}