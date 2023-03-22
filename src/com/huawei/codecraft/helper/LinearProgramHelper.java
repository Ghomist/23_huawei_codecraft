package com.huawei.codecraft.helper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.huawei.codecraft.math.HalfPlane;
import com.huawei.codecraft.math.Vector2;

public class LinearProgramHelper {

    // public static final double RVO_EPSILON = 0.00001;
    // public static final double MAX_SPEED = Robot.MAX_FORWARD_SPEED;
    // public static Vector2 newVelocity;

    public static Vector2 shortestDistance(List<HalfPlane> planes, Vector2 pos) {
        if (planes.size() == 0) {
            return Vector2.ZERO;
        }

        List<Vector2> paths = new LinkedList<>();
        for (HalfPlane plane : planes) {
            if (!plane.isOnPlane(pos)) {
                paths.add(plane.shortestPath(pos));
            }
        }

        // 点在所有的半平面内
        if (paths.isEmpty()) {
            return Vector2.ZERO;
        }

        // 垂线里面最短的
        paths.sort((a, b) -> Double.compare(a.length2(), b.length2()));
        for (Vector2 path : paths) {
            Vector2 testPos = pos.add(path);
            boolean allIn = true;
            for (HalfPlane plane : planes) {
                if (!plane.isOnPlane(testPos)) {
                    allIn = false;
                    break;
                }
            }
            if (allIn) {
                return path;
            }
        }

        // 找出所有边线的交点
        List<Vector2> corners = new LinkedList<>();
        for (int i = 0; i < planes.size(); ++i) {
            HalfPlane a = planes.get(i);
            for (int j = i + 1; j < planes.size(); ++j) {
                HalfPlane b = planes.get(j);
                Vector2 intersection = a.line.intersection(b.line);
                if (intersection != null)
                    corners.add(intersection);
            }
        }
        // 去除不能到达的（不在半平面交上面的）
        Iterator<Vector2> it = corners.iterator();
        while (it.hasNext()) {
            Vector2 corner = it.next();
            boolean allIn = true;
            for (HalfPlane plane : planes) {
                if (!plane.isOnPlane(corner)) {
                    allIn = false;
                    break;
                }
            }
            if (!allIn) {
                it.remove();
            }
        }
        // 这里半平面没有办法形成正常的交集，返回零向量
        if (corners.size() == 0) {
            return Vector2.ZERO;
        }

        // 找到离目标最近的交点
        Vector2 closestPath = corners.get(0).subtract(pos);
        double closestDist2 = closestPath.length2();
        for (Vector2 corner : corners) {
            Vector2 path = corner.subtract(pos);
            double dist2 = path.length2();
            if (dist2 < closestDist2) {
                closestPath = path;
                closestDist2 = dist2;
            }
        }
        return closestPath;
    }
}