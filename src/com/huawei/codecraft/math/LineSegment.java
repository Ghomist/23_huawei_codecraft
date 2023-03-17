package com.huawei.codecraft.math;

public class LineSegment {
    private Vector2 p1;
    private Vector2 p2;

    private double k; // 斜率
    private double b; // 截距

    public LineSegment(Vector2 p1, Vector2 p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.k = (p2.y - p1.y) / (p2.x - p1.x);
        this.b = p1.y - k * p1.x;
    }

    public double getSlope() {
        return k;
    }

    public double getIntercept() {
        return b;
    }

    public double getLength() {
        return Vector2.distance(p2, p1);
    }

    public Vector2 getMidPoint() {
        return new Vector2((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    public static LineSegment shortestPath(Vector2 point, LineSegment line) {
        Vector2 lineStart = line.p1;
        Vector2 lineEnd = line.p2;
        double dx = lineEnd.x - lineStart.x;
        double dy = lineEnd.y - lineStart.y;

        // 如果线段的长度为0，则返回点到线段起点的距离
        if (dx == 0 && dy == 0) {
            return new LineSegment(point, lineStart);
        }

        // 计算线段的长度的平方
        double lineLengthSq = dx * dx + dy * dy;

        // 计算点在线段上的投影点
        double t = ((point.x - lineStart.x) * dx + (point.y - lineStart.y) * dy) / lineLengthSq;
        if (t > 1) {
            t = 1;
        } else if (t < 0) {
            t = 0;
        }
        Vector2 projection = new Vector2(lineStart.x + t * dx, lineStart.y + t * dy);

        // 返回点到投影点的距离
        return new LineSegment(point, projection);
    }

    public static LineSegment shortestPath(LineSegment a, LineSegment b) {
        LineSegment ret = shortestPath(a.p1, b);
        LineSegment d2 = shortestPath(a.p2, b);
        if (d2.getLength() < ret.getLength())
            ret = d2;
        LineSegment d3 = shortestPath(b.p1, a);
        if (d3.getLength() < ret.getLength())
            ret = d3;
        LineSegment d4 = shortestPath(b.p2, a);
        if (d4.getLength() < ret.getLength())
            ret = d4;
        return ret;
    }

    public static Vector2 lineIntersection(LineSegment a, LineSegment b, double maxDistance) {
        Vector2 p1 = a.p1;
        Vector2 p2 = a.p2;
        Vector2 p3 = b.p1;
        Vector2 p4 = b.p2;

        // 每条线段的斜率和截距
        double slope1 = a.getSlope();
        double intercept1 = a.getIntercept();

        double slope2 = b.getSlope();
        double intercept2 = a.getIntercept();

        // 如果两条线段的斜率相同，则它们平行，没有交点
        if (slope1 == slope2) {
            return null;
        }

        // 计算交点的x和y坐标
        double x = (intercept2 - intercept1) / (slope1 - slope2);
        double y = slope1 * x + intercept1;

        // 检查交点是否在两个线段上
        if (x >= Math.min(p1.x, p2.x) && x <= Math.max(p1.x, p2.x) &&
                y >= Math.min(p1.y, p2.y) && y <= Math.max(p1.y, p2.y) &&
                x >= Math.min(p3.x, p4.x) && x <= Math.max(p3.x, p4.x) &&
                y >= Math.min(p3.y, p4.y) && y <= Math.max(p3.y, p4.y))
            return new Vector2(x, y);
        else {
            LineSegment shortestPath = shortestPath(a, b);
            if (shortestPath.getLength() <= maxDistance) {
                return shortestPath.getMidPoint();
            } else {
                return null;
            }
        }
    }

    public static Vector2 lineIntersection(LineSegment a, LineSegment b) {
        Vector2 p1 = a.p1;
        Vector2 p2 = a.p2;
        Vector2 p3 = b.p1;
        Vector2 p4 = b.p2;

        // 每条线段的斜率和截距
        double slope1 = a.getSlope();
        double intercept1 = a.getIntercept();

        double slope2 = b.getSlope();
        double intercept2 = a.getIntercept();

        // 如果两条线段的斜率相同，则它们平行，没有交点
        if (slope1 == slope2) {
            return null;
        }

        // 计算交点的x和y坐标
        double x = (intercept2 - intercept1) / (slope1 - slope2);
        double y = slope1 * x + intercept1;

        // 检查交点是否在两个线段上
        if (x >= Math.min(p1.x, p2.x) && x <= Math.max(p1.x, p2.x) &&
                y >= Math.min(p1.y, p2.y) && y <= Math.max(p1.y, p2.y) &&
                x >= Math.min(p3.x, p4.x) && x <= Math.max(p3.x, p4.x) &&
                y >= Math.min(p3.y, p4.y) && y <= Math.max(p3.y, p4.y))
            return new Vector2(x, y);

        // 如果交点不在两个线段上，则返回null
        return null;
    }
}
