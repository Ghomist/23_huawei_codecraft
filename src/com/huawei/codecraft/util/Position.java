package com.huawei.codecraft.util;

public class Position {
    public float x;
    public float y;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Position GetPosFromGridIndex(int x, int y) {
        return new Position(x * 0.5f + 0.25f, y * 0.5f + 0.25f);
    }
}
