package com.huawei.codecraft.entity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.huawei.codecraft.helper.MathHelper;
import com.huawei.codecraft.math.Vector2;

public class GameMap {

    private int[][] grids = new int[100][100];

    public void setGrid(int x, int y, int type) {
        grids[x][y] = type;
    }

    public int get(int x, int y) {
        return grids[x][y];
    }

    /**
     * A* 寻路
     * @param from 起始点坐标
     * @param to 终点坐标
     */
    public List<Vector2> findPath(Vector2 from, Vector2 to) {
        int[] start = toGridPos(from);
        int[] end = toGridPos(to);

        Set<GameMapNode> visited = new HashSet<>();
        PriorityQueue<GameMapNode> visiting = new PriorityQueue<>();
        GameMapNode beginNode = GameMapNode.makeStartNode(start, end);
        visiting.add(beginNode);
        visited.add(beginNode);

        GameMapNode endNode = null;

        while (visiting.size() > 0) {
            // choose best node
            GameMapNode choice = visiting.poll();

            if (choice.x == start[0] && choice.y == start[1]) {
                // arrive
                endNode = choice;
                break;
            }

            // 8 dir expand
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    if (i == 0 && j == 0)
                        continue;
                    GameMapNode move = choice.makeMove(this, i, j, end);
                    if (move != null && visited.add(move)) {
                        visiting.add(move);
                    }
                }
            }
        }

        if (endNode != null) {
            LinkedList<Vector2> ans = new LinkedList<>();

            ans.add(endNode.getPos());
            while (endNode.pre != null) {
                ans.addFirst(endNode.pre.getPos());
                endNode = endNode.pre;
            }

            return smoothPath(ans);
        } else {
            // no path
            return null;
        }
    }

    private List<Vector2> smoothPath(List<Vector2> path) {
        // TODO: smooth path
        return path;
    }

    private int[] toGridPos(Vector2 pos) { // TODO: to be confirmed
        int[] ret = new int[2];
        ret[0] = MathHelper.round(pos.x * 2);
        ret[1] = MathHelper.round(pos.y * 2);
        return ret;
    }
}
