package com.huawei.codecraft.entity;

import com.huawei.codecraft.math.Vector2;
import com.huawei.codecraft.util.BitCalculator;

public class Workbench {

    public int id;

    private int type;
    private Vector2 pos;
    private int remainFrames;
    public boolean hasProduction;
    private int materialStatus;

    private boolean[] pendingMaterial = new boolean[10];
    private boolean isOrdered = false;

    public Workbench(int id, int type, Vector2 pos) {
        this.pos = pos;
        this.id = id;
        this.type = type;
    }

    public void update(String info) {
        String[] infos = info.split(" ");
        type = infos[0].charAt(0) - '0';

        double x = Double.parseDouble(infos[1]);
        double y = Double.parseDouble(infos[2]);
        pos = new Vector2(x, y);

        remainFrames = Integer.parseInt(infos[3]);
        materialStatus = Integer.parseInt(infos[4]);
        hasProduction = infos[5].charAt(0) == '1';
    }

    public boolean isDangerous() {
        return (pos.x <= 3 || pos.x >= 48 || pos.y <= 3 || pos.y >= 48);
    }

    public int getType() {
        return type;
    }

    public Vector2 getPos() {
        return pos;
    }

    public int getRemainFrames() {
        return remainFrames;
    }

    public boolean isProducing() {
        return remainFrames > 0;
    }

    public boolean isProducingOrFinish() {
        return remainFrames >= 0;
    }

    public boolean hasProduction() {
        return hasProduction;
    }

    public boolean hasMaterial(int item) {
        if (type >= 8 || type <= 3)
            return false;
        return BitCalculator.isOne(materialStatus, item) || pendingMaterial[item];
    }

    /**
     * 获取订单状态
     * 
     * @return 是否有机器人要取物品
     */
    public boolean isOrdered() {
        return isOrdered;
    }

    /**
     * 要取物品时调用，防止其它机器人也来同一个工作台抢物品
     */
    public void order() {
        isOrdered = true;
    }

    /**
     * 取消订单时调用
     */
    public void cancelOrder() {
        isOrdered = false;
    }

    public void setPendingMaterial(int material) {
        pendingMaterial[material] = true;
    }

    public void finishPendingMaterial(int material) {
        pendingMaterial[material] = false;
        BitCalculator.setOne(materialStatus, material);
    }

    public void cancelPendingMaterial(int material) {
        pendingMaterial[material] = false;
    }

    /**
     * 根据缺失的原料计算该工作台的权重
     */
    @Deprecated
    public int missingMaterialWeight() {
        switch (type) {
            case 4:
                return (hasMaterial(1) ^ hasMaterial(2)) ? 1 : 0;
            case 5:
                return (hasMaterial(1) ^ hasMaterial(3)) ? 1 : 0;
            case 6:
                return (hasMaterial(2) ^ hasMaterial(3)) ? 1 : 0;
            case 7:
                boolean a = hasMaterial(4);
                boolean b = hasMaterial(5);
                boolean c = hasMaterial(6);
                if (a && b && !c || a && !b && c || !a && b && c)
                    return 2;
                else if (a && !b && !c || !a && !b && c || !a && b && !c)
                    return 1;
                else
                    return 0;
            case 8:
                return 2;
            default:
                return 0;
        }
    }
}
