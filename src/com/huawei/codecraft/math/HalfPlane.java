package com.huawei.codecraft.math;

public class HalfPlane {
    public Line line;
    public Vector2 dir;

    public HalfPlane() {
    }

    public HalfPlane(Line line, Vector2 dir) {
        this.dir = dir;
        this.line = line;
    }

    public boolean isOnPlane(Vector2 pos) {
        Vector2 v = pos.subtract(line.point);
        return v.dot(dir) > 0;
    }
}
