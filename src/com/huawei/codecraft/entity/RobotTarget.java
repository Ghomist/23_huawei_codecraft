package com.huawei.codecraft.entity;

import com.huawei.codecraft.math.Vector2;

public class RobotTarget {
    public Vector2 pos;
    public CraftTable table;

    public RobotTarget(Vector2 pos) {
        this.pos = pos;
    }

    public RobotTarget(CraftTable table) {
        this.pos = table.getPos();
        this.table = table;
    }
}
