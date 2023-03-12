package com.huawei.codecraft.entity;

import java.util.List;

import com.huawei.codecraft.util.BitCalculator;
import com.huawei.codecraft.util.Vector2;

public class CraftTable {

    public int id;

    private int type;
    private Vector2 pos;
    private int remainFrames;
    private boolean hasProduction;
    private int materialStatus;

    public CraftTable(int id) {
        this.pos = new Vector2(0, 0);
        this.id = id;
    }

    public void update(String info) {
        String[] infos = info.split(" ");
        type = infos[0].charAt(0) - '0';

        float x = Float.parseFloat(infos[1]);
        float y = Float.parseFloat(infos[2]);
        pos.set(x, y);

        remainFrames = Integer.parseInt(infos[3]);
        materialStatus = Integer.parseInt(infos[4]);
        hasProduction = infos[5].charAt(0) == '1';
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

    public boolean hasMaterial(int item) {
        return BitCalculator.isOne(materialStatus, item);
    }

    public List<Integer> inNeedMaterials() {
        // Todo
        return null;
    }

    public boolean hasProduction() {
        return hasProduction;
    }
}
