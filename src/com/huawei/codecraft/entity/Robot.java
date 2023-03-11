package com.huawei.codecraft.entity;

import java.util.LinkedList;
import java.util.List;

import com.huawei.codecraft.util.ItemPriceHelper;
import com.huawei.codecraft.util.Position;

public class Robot {
    public int id;

    private int tableID;
    private int item;
    private float timeValueArg;
    private float impactValueArg;
    private float w;
    private float v;
    private float dir;
    private Position pos;

    private List<String> cmdList = new LinkedList<>();

    public Robot(Position pos) {
        this.pos = pos;
    }

    public void update(int id, String info) {
        this.id = id;

        String[] parts = info.split(" ");

        tableID = Integer.parseInt(parts[0]);
        item = parts[1].charAt(1) - '0';
        timeValueArg = Float.parseFloat(parts[2]);
        impactValueArg = Float.parseFloat(parts[3]);
        w = Float.parseFloat(parts[4]);
        v = Float.parseFloat(parts[5]);
        dir = Float.parseFloat(parts[6]);

        float x = Float.parseFloat(parts[7]);
        float y = Float.parseFloat(parts[8]);
        pos.set(x, y);

        cmdList.clear();
    }

    public void setForwardSpeed(float speed) {
        cmdList.add("forward " + id + ' ' + speed);
    }

    public void setRotateSpeed(float speed) {
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

    public int getTable() {
        return tableID;
    }

    public boolean hasItem() {
        return item != 0;
    }

    public int getItem() {
        return item;
    }

    public float getItemPrice() {
        return ItemPriceHelper.getSalePrice(item) * timeValueArg * impactValueArg;
    }

    public float getAngleSpeed() {
        return w;
    }

    public float getLineSpeed() {
        return v;
    }

    public float getDir() {
        return dir;
    }

    public List<String> getCommands() {
        return cmdList;
    }
}
