package com.huawei.codecraft.entity;

import com.huawei.codecraft.math.Vector2;

public class RobotTarget {
    public Vector2 pos;
    public Workbench bench;

    public RobotTarget(Vector2 pos) {
        this.pos = pos;
    }

    public RobotTarget(Workbench bench) {
        this.pos = bench.getPos();
        this.bench = bench;
    }
}
