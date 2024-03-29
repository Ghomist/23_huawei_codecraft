package com.huawei.codecraft.math;

import com.huawei.codecraft.helper.MathHelper;

public class Vector2 {
    public static final Vector2 ZERO = new Vector2();
    public static final Vector2 UP = new Vector2(0, 1);
    public static final Vector2 DOWN = new Vector2(0, -1);
    public static final Vector2 LEFT = new Vector2(-1, 0);
    public static final Vector2 RIGHT = new Vector2(1, 0);

    public final double x;
    public final double y;

    public Vector2() {
        x = 0;
        y = 0;
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 v) {
        x = v.x;
        y = v.y;
    }

    public Vector2 add(Vector2 v) {
        return new Vector2(v.x + x, v.y + y);
    }

    public Vector2 add(double k, Vector2 v) {
        return new Vector2(x + v.x * k, y + v.y * k);
    }

    public Vector2 subtract(Vector2 v) {
        return new Vector2(x - v.x, y - v.y);
    }

    public Vector2 subtract(double k, Vector2 v) {
        return new Vector2(x - v.x * k, y - v.y * k);
    }

    public Vector2 multiply(double n) {
        return new Vector2(x * n, y * n);
    }

    public double dot(Vector2 v) {
        return x * v.x + y * v.y;
    }

    public double cross(Vector2 v) {
        return x * v.y - y * v.x;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double length2() {
        return x * x + y * y;
    }

    public double radian() {
        return Math.atan2(y, x);
    }

    public Vector2 normalize() {
        double len = length();
        return new Vector2(x / len, y / len);
    }

    public Vector2 copy() {
        return new Vector2(x, y);
    }

    public Vector2Int toGrid() {
        return new Vector2Int((int) (x * 2), (int) (y * 2));
    }

    public static Vector2 getPosFromGridIndex(int x, int y) {
        return new Vector2(y * 0.5f + 0.25f, 49.75f - x * 0.5f);
    }

    public static Vector2 getFromRadian(double radius) {
        return new Vector2(Math.cos(radius), Math.sin(radius));
    }

    public static Vector2 getFromRadian(double radius, double length) {
        return getFromRadian(radius).multiply(length);
    }

    public static double cross(Vector2 a, Vector2 b) {
        return a.x * b.y - a.y * b.x;
    }

    public static double distance2(Vector2 a, Vector2 b) {
        double x = a.x - b.x;
        double y = a.y - b.y;
        return x * x + y * y;
    }

    public static double distance(Vector2 a, Vector2 b) {
        double x = a.x - b.x;
        double y = a.y - b.y;
        return Math.sqrt(x * x + y * y);
    }

    public static double cos(Vector2 a, Vector2 b) {
        return a.dot(b) / (a.length() * b.length());
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
