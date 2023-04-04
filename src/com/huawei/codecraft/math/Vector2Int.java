package com.huawei.codecraft.math;

import java.util.Objects;

public class Vector2Int {
    public static final Vector2Int ZERO = new Vector2Int(0, 0);

    public final int x;
    public final int y;

    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Int offset(int x, int y) {
        return new Vector2Int(this.x + x, this.y + y);
    }

    public Vector2 toPos() {
        return new Vector2(x / 2.0 + 0.25, y / 2.0 + 0.25);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Vector2Int))
            return false;
        Vector2Int o = (Vector2Int) obj;
        return x == o.x && y == o.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
