package com.huawei.codecraft.entity;

import com.huawei.codecraft.math.Vector2;

class GameMapNode implements Comparable<GameMapNode> {
    public final int x;
    public final int y;
    public final double g;
    public final double h;
    public final double f;
    public final GameMapNode pre;

    private GameMapNode(int[] pos, int[] end, GameMapNode pre) {
        x = pos[0];
        y = pos[1];
        if (pre == null) {
            g = 0;
        } else {
            g = pre.g + (pre.x == x || pre.y == y ? 1 : 1.414);
        }
        h = Math.pow(pos[0] - end[0], 2) + Math.pow(pos[1] - end[1], 2);
        f = g + h;
        this.pre = pre;
    }

    static GameMapNode makeStartNode(int[] start, int[] end) {
        return new GameMapNode(start, end, null);
    }

    GameMapNode makeMove(GameMap map, int moveX, int moveY, int[] end) {
        int x = this.x + moveX;
        int y = this.y + moveY;
        if (x < 0 || x >= 100 || y < 0 || y >= 100)
            return null;
        if (map.get(x, y) == -1)
            return null;
        return new GameMapNode(new int[] { x, y }, end, this);
    }

    Vector2 getPos() {
        return new Vector2(x / 2.0 + 0.25, y / 2.0 + 0.25); // TODO: need confirm
    }

    @Override
    public boolean equals(Object other) {
        GameMapNode o = (GameMapNode) other;
        if (o != null) {
            return x == o.x && y == o.y;
        }
        return false;
    }

    @Override
    public int compareTo(GameMapNode o) {
        // choose max f node
        return -Double.compare(f, o.f);
    }
}