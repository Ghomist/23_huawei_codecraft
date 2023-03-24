package com.huawei.codecraft.entity;

import com.huawei.codecraft.math.Vector2;
import com.huawei.codecraft.util.BitCalculator;

public class Workbench {

    public int id;

    private int type;
    private Vector2 pos;
    private int remainFrames;
    private boolean hasProduction;
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

    public boolean isFree() {
        return remainFrames == -1;
    }

    public boolean hasFinished() {
        return remainFrames == 0;
    }

    public boolean isProducingOrFinish() {
        return remainFrames >= 0;
    }

    public boolean hasMaterial(int item) {
        return BitCalculator.isOne(materialStatus, item) || pendingMaterial[item];
    }

    public boolean hasProduction() {
        return hasProduction;
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public void setOrder(boolean order) {
        isOrdered = order;
    }

    public void setPendingMaterial(int material) {
        pendingMaterial[material] = true;
    }

    public void finishPendingMaterial(int material) {
        pendingMaterial[material] = false;
    }

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
            default:
                return 0;
        }
    }
}
