package com.huawei.codecraft.entity;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import com.huawei.codecraft.helper.MathHelper;
import com.huawei.codecraft.io.Output;
import com.huawei.codecraft.math.Vector2;

public class GameMap {

    public static final int OBSTACLE = -1;
    public static final int EMPTY = 0;

    private int[][] grids = new int[100][100];
    private List<Vector2> obstacles = new LinkedList<>();

    public GameMap(int[][] grids) {
        for (int x = 0; x < 100; ++x) {
            for (int y = 0; y < 100; ++y) {
                this.grids[x][y] = grids[99 - y][x];
                if (this.grids[x][y] == OBSTACLE) {
                    obstacles.add(new Vector2(x / 2.0 + 2.5, y / 2.0 + 2.5));
                }
            }
        }
    }

    public int get(int x, int y) {
        return grids[x][y];
    }

    public List<Vector2> getObstacles() {
        return obstacles;
    }

    /**
     * 寻找网格上的两点的直线是否存在障碍物
     * 
     * @return {@code true} 如果存在障碍物
     */
    public boolean hasObstacle(int x1, int y1, int x2, int y2) { // TODO: 有问题不能使用
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int x = x1;
        int y = y1;
        int k = dy > dx ? dx * 2 : dy * 2;
        int e = k - (dy > dx ? dx : dy);
        int incX = x2 > x1 ? 1 : -1;
        int incY = y2 > y1 ? 1 : -1;
        for (int i = 0; i < k; i++) {
            if (grids[x][y] == OBSTACLE) {
                return false;
            }
            if (e > 0) {
                if (dy > dx) {
                    y += incY;
                } else {
                    x += incX;
                }
                e -= 2 * (dy > dx ? dx : dy);
            }
            if (dy > dx) {
                x += incX;
            } else {
                y += incY;
            }
            e += 2 * (dy > dx ? dy : dx);
            if (x == x2 && y == y2) {
                return true;
            }
        }
        return false;
    }
    

    private boolean hasObstacle(GameMapNode node1, GameMapNode node2) {
        return hasObstacle(node1.x, node1.y, node2.x, node2.y);
    }

    /**
     * 正常地图的 A* 寻路
     * 
     * @param from 起始点坐标
     * @param to   终点坐标
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

            if (choice.x == end[0] && choice.y == end[1]) {
                // arrive
                endNode = choice;
                break;
            }

            // 8 dir expand
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    if (i == 0 && j == 0)
                        continue;
                    GameMapNode move = choice.makeMove(grids, i, j, end);
                    if (move != null) {
                        if (visited.add(move)) {
                            visiting.add(move);
                        } else {
                            // update the G value
                            Iterator<GameMapNode> it = visited.iterator();
                            while (it.hasNext()) {
                                GameMapNode theMove = it.next();
                                if (move.equals(theMove)) {
                                    theMove.tryUpdateByPrevious(grids, choice);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (endNode != null) {
            LinkedList<GameMapNode> ans = new LinkedList<>();

            ans.add(endNode);
            while (endNode.pre != null) {
                ans.addFirst(endNode.pre);
                endNode = endNode.pre;
            }

            // for (var a : ans) {
            // grids[a.x][a.y] = 10;
            // }

            // try (FileWriter fw = new FileWriter("aaaaa.txt")) {
            // for (var a : grids) {
            // for (var b : a) {
            // char c = ' ';
            // if (b == -1)
            // c = '#';
            // else if (b == 10)
            // c = 'X';
            // fw.append(c);
            // }
            // fw.append('\n');
            // }
            // } catch (Exception e) {
            // }

            return smoothPath(ans);
        } else {
            // no path
            return null;
        }
    }

    private List<Vector2> smoothPath(List<GameMapNode> path) {
        // Iterator<GameMapNode> it = path.iterator();
        // GameMapNode current = it.next();
        // while (it.hasNext()) {
        //     GameMapNode next = it.next();
        //     // if (!hasObstacle(current, next)) {
        //     if (current.x == next.x || current.y == next.y) {
        //         // 移除多余的顶点
        //         it.remove();
        //     } else {
        //         // 从新的顶点开始计算
        //         current = next;
        //     }
        // }
        return path.stream()
                .map(x -> x.getPos())
                .collect(Collectors.toList());
    }

    private int[] toGridPos(Vector2 pos) { // TODO: to be confirmed
        int[] ret = new int[2];
        ret[0] = MathHelper.round(pos.x * 2);
        ret[1] = MathHelper.round(pos.y * 2);
        return ret;
    }
}
