package com.huawei.codecraft.math;

public class Line {
    public Vector2 point;
    public Vector2 dir;

    public Line() {
        point = new Vector2(0, 0);
        dir = new Vector2(0, 0);
    }

    public Line(Vector2 pos, Vector2 dir) {
        this.point = pos;
        this.dir = dir;
    }

    public double distanceTo(Vector2 point) {
        Vector2 v1 = point.subtract(this.point);
        Vector2 normal = new Vector2(-this.dir.y, this.dir.x);
        double d = v1.dot(normal) / this.dir.dot(normal);
        Vector2 v2 = this.dir.multiply(d);
        Vector2 v3 = v1.subtract(v2);
        return v3.length();
    }
}
