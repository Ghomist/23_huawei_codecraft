package com.huawei.codecraft.util;

public class Vector2 {
    public float x;
    public float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 getPosFromGridIndex(int x, int y) {
        return new Vector2(x * 0.5f + 0.25f, y * 0.5f + 0.25f);
    }

    public static double distance(Vector2 a, Vector2 b) {
        float x = a.x - b.x;
        float y = a.y - b.y;
        return Math.sqrt(x * x + y * y);
    }
}
