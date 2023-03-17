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

    public Vector2 shortestPath(Vector2 pos) {
        return isOnPlane(pos) ? Vector2.ZERO : line.getProjection(pos).subtract(pos);
    }

    public double shortestDist(Vector2 pos) {
        return isOnPlane(pos) ? 0 : line.distanceTo(pos);
    }

    public boolean isOnPlane(Vector2 pos) {
        Vector2 v = pos.subtract(line.point);
        return v.dot(dir) >= 0;
    }
}
