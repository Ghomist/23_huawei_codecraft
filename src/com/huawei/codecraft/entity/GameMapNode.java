package com.huawei.codecraft.entity;

import java.util.Objects;

import com.huawei.codecraft.helper.ArrayHelper;
import com.huawei.codecraft.math.Vector2;
import com.huawei.codecraft.math.Vector2Int;

@Deprecated
class GameMapNode implements Comparable<GameMapNode> {
    public final int x;
    public final int y;
    public double g;
    public double h;
    public double f;
    public GameMapNode pre;

    private GameMapNode(Vector2Int pos, Vector2Int end, GameMapNode pre, boolean nearByWall) {
        x = pos.x;
        y = pos.y;
        if (pre == null) {
            g = 0;
        } else {
            g = pre.g + (pre.x == x || pre.y == y ? 1 : 1.414);
        }
        // h = Math.sqrt(Math.pow(pos.x - end.x, 2) + Math.pow(pos.y - end.y, 2));
        h = Math.abs(pos.x - end.x) + Math.abs(pos.y - end.y);
        if (nearByWall)
            h *= 5; // use node near by wall less
        f = g + h;
        this.pre = pre;
    }

    public static GameMapNode makeStartNode(Vector2Int start, Vector2Int end) {
        return new GameMapNode(start, end, null, false);
    }

    public GameMapNode makeMove(int[][] map, int moveX, int moveY, Vector2Int end, boolean strict) {
        int x = this.x + moveX;
        int y = this.y + moveY;
        if (ArrayHelper.safeGet(map, x, y, GameMap.OBSTACLE) == GameMap.OBSTACLE)
            return null;
        int cnt = nearByWallCount(map, x, y);
        if ((strict && cnt >= 1) || cnt >= 2)
            return null;
        return new GameMapNode(new Vector2Int(x, y), end, this, isNearByWall(map, x, y));
    }

    public void tryUpdateByPrevious(int[][] map, GameMapNode pre) {
        double newG = pre.g + (pre.x == x || pre.y == y ? 1 : 1.414);
        if (newG < g) {
            g = newG;
            f = g + h;
            this.pre = pre;
        }
    }

    public Vector2 getPos() {
        return new Vector2(x / 2.0 + 0.25, y / 2.0 + 0.25); // TODO: need confirm
    }

    private int nearByWallCount(int[][] map, int x, int y) {
        int ret = 0;
        if (ArrayHelper.safeGet(map, x + 1, y, GameMap.OBSTACLE) == GameMap.OBSTACLE) {
            ret++;
        }
        if (ArrayHelper.safeGet(map, x - 1, y, GameMap.OBSTACLE) == GameMap.OBSTACLE) {
            ret++;
        }
        if (ArrayHelper.safeGet(map, x, y + 1, GameMap.OBSTACLE) == GameMap.OBSTACLE) {
            ret++;
        }
        if (ArrayHelper.safeGet(map, x, y - 1, GameMap.OBSTACLE) == GameMap.OBSTACLE) {
            ret++;
        }
        return ret;
    }

    private boolean isNearByWall(int[][] map, int x, int y) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (ArrayHelper.safeGet(map, x + i, y + j, GameMap.OBSTACLE) == GameMap.OBSTACLE) {
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