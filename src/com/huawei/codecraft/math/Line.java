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
        Vector2 p1 = point, v1 = dir, p2 = other.point, v2 = other.dir;
        double cross = v1.cross(v2);
        if (cross == 0) { // 两条直线平行
            return null;
        } else {
            Vector2 diff = p2.subtract(p1);
            double t1 = diff.cross(v2) / cross;
            return p1.add(v1.multiply(t1));
        }
    }

    public double distanceTo(Vector2 point) { // Todo
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
