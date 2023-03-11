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

    public static Vector2 GetPosFromGridIndex(int x, int y) {
        return new Vector2(x * 0.5f + 0.25f, y * 0.5f + 0.25f);
    }
}
