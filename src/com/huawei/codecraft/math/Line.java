package com.huawei.codecraft.math;

public class Line {
    public Vector2 point;
    public Vector2 dir;

    public Line() {
        point = Vector2.ZERO;
        dir = Vector2.ZERO;
    }

    public Line(Vector2 pos, Vector2 dir) {
        this.point = pos;
        this.dir = dir;
    }

    public double getK() {
        return dir.y / dir.x;
    }

    public Vector2 intersection(Line other) {
        double x1 = point.x;
        double y1 = point.y;
        double x2 = other.point.x;
        double y2 = other.point.y;
        double k1 = getK();
        double k2 = other.getK();
        double x = (y1 - y2 - k1 * x1 + k2 * x2) / (k2 - k1);
        double y = y1 - k1 * (x1 - x);
        return new Vector2(x, y);
    }

    public double distanceTo(Vector2 point) { // TODO
        Vector2 v1 = point.subtract(this.point);
        Vector2 normal = new Vector2(-this.dir.y, this.dir.x);
        double d = v1.dot(normal) / this.dir.dot(normal);
        Vector2 v2 = this.dir.multiply(d);
        Vector2 v3 = v1.subtract(v2);
        return v3.length();
    }

    public Vector2 getProjection(Vector2 p) {
        return new Line(p, new Vector2(-dir.y, dir.x)).intersection(this);
    }
}
