package com.huawei.codecraft.util;

public class Vector2 {
    public double x;
    public double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(double x, double y) {
        this.x += x;
        this.y += y;
    }

    public void multiply(double n) {
        x *= n;
        y *= n;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public static Vector2 getPosFromGridIndex(int x, int y) {
        return new Vector2(y * 0.5f + 0.25f, 49.75f - x * 0.5f);
    }

    public static Vector2 getFromRadius(double radius) {
        return new Vector2(Math.cos(radius), Math.sin(radius));
    }

    public static Vector2 getFromRadius(double radius, double length) {
        Vector2 v = getFromRadius(radius);
        v.x *= length;
        v.y *= length;
        return v;
    }

    public static double distance(Vector2 a, Vector2 b) {
        double x = a.x - b.x;
        double y = a.y - b.y;
        return Math.sqrt(x * x + y * y);
    }

    public static Vector2 add(Vector2 a, Vector2 b) {
        return new Vector2(a.x + b.x, a.y + b.y);
    }

    public static Vector2 dot(Vector2 a, Vector2 b) {
        return new Vector2(a.x * b.x, a.y * b.y);
    }

    public static Vector2 dot(Vector2 v, double n) {
        return new Vector2(v.x * n, v.y * n);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
