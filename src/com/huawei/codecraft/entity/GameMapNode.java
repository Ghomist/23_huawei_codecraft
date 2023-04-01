package com.huawei.codecraft.entity;

import java.util.Objects;

import com.huawei.codecraft.math.Vector2;

class GameMapNode implements Comparable<GameMapNode> {
    public final int x;
    public final int y;
    public double g;
    public final double h;
    public double f;
    public GameMapNode pre;

    private GameMapNode(int[] pos, int[] end, GameMapNode pre, boolean nearByWall) {
        x = pos[0];
        y = pos[1];
        if (pre == null) {
            g = 0;
        } else {
            double g = pre.g + (pre.x == x || pre.y == y ? 1 : 1.414);
            this.g = nearByWall ? g * 5 : g; // use node near by wall less
        }
        h = Math.sqrt(Math.pow(pos[0] - end[0], 2) + Math.pow(pos[1] - end[1], 2));
        f = g + h;
        this.pre = pre;
    }

    public static GameMapNode makeStartNode(int[] start, int[] end) {
        return new GameMapNode(start, end, null, false);
    }

    public GameMapNode makeMove(int[][] map, int moveX, int moveY, int[] end) {
        int x = this.x + moveX;
        int y = this.y + moveY;
        if (x < 0 || x >= 100 || y < 0 || y >= 100)
            return null;
        if (map[x][y] == -1)
            return null;
        boolean nearByWall = isNearByWall(map, x, y);
        return new GameMapNode(new int[] { x, y }, end, this, nearByWall);
    }

    public void tryUpdateByPrevious(int[][] map, GameMapNode pre) {
        double newG = pre.g + (pre.x == x || pre.y == y ? 1 : 1.414);
        boolean nearByWall = isNearByWall(map, x, y);
        newG = nearByWall ? newG * 5 : newG;
        if (newG < g) {
            g = newG;
            f = g + h;
            this.pre = pre;
        }
    }

    public Vector2 getPos() {
        return new Vector2(x / 2.0 + 0.25, y / 2.0 + 0.25); // TODO: need confirm
    }

    private boolean isNearByWall(int[][] map, int x, int y) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (x + i < 0 || x + i >= 100 || y + j < 0 || y + j >= 100)
                    continue;
                if (map[x + i][y + j] == -1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (!(other instanceof GameMapNode))
            return false;
        GameMapNode o = (GameMapNode) other;
        return x == o.x && y == o.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(GameMapNode o) {
        // choose min f node
        return Double.compare(f, o.f);
    }
}