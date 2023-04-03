package com.huawei.codecraft.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import com.huawei.codecraft.helper.MathHelper;
import com.huawei.codecraft.math.Vector2;

public class GameMap {

    public static final int OBSTACLE = -1;
    public static final int EMPTY = 0;

    private int[][] grids = new int[100][100];
    private int[][] map1 = new int[100][100];
    private int[][] map2 = new int[100][100];
    private final Vector2[] obstacles;

    public GameMap(int[][] grids) {
        List<Vector2> obs = new LinkedList<>();
        for (int x = 0; x < 100; ++x) {
            for (int y = 0; y < 100; ++y) {
                this.grids[x][y] = grids[99 - y][x];
                if (this.grids[x][y] == OBSTACLE) {
                    obs.add(new Vector2(x / 2.0 + 2.5, y / 2.0 + 2.5));
                }
            }
        }
        obstacles = obs.toArray(Vector2[]::new);
        for (int x = 0; x < 100; ++x) {
            for (int y = 0; y < 100; ++y) {
                boolean u = false, d = false, l = false, r = false;
                // gen map 1
                if (safeGet(x, y) != OBSTACLE)
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            if (safeGet(x + i, y + j) == OBSTACLE) {
                                if (i < 0)
                                    l = true;
                                else if (i > 0)
                                    r = true;
                                if (j < 0)
                                    d = true;
                                else if (j > 0)
                                    u = true;
                            }
                        }
                    }
                map1[x][y] = (u && d || l && r) ? OBSTACLE : grids[x][y];
            }
        }
    }

    public void safeSet(int x, int y, int v) {
        if (x < 0 || x >= 100 || y < 0 || y >= 100)
            return;
        grids[x][y] = v;
    }

    public int safeGet(int x, int y) {
        if (x < 0 || x >= 100 || y < 0 || y >= 100)
            return OBSTACLE;
        return grids[x][y];
    }

    public Vector2[] getObstacles() {
        return obstacles;
    }

    // TODO: 有问题不能使用
    /**
     * 寻找网格上的两点的直线是否存在障碍物
     * 
     * @return {@code true} 如果存在障碍物
     */
    public boolean hasObstacle(int x1, int y1, int x2, int y2) {
        int x = x1;
        int y = y1;

        int w = x2 - x1;
        int h = y2 - y1;

        int dx1 = w < 0 ? -1 : (w > 0 ? 1 : 0);
        int dy1 = h < 0 ? -1 : (h > 0 ? 1 : 0);

        int dx2 = w < 0 ? -1 : (w > 0 ? 1 : 0);
        int dy2 = 0;

        int fastStep = Math.abs(w);
        int slowStep = Math.abs(h);
        if (fastStep <= slowStep) {
            fastStep = Math.abs(h);
            slowStep = Math.abs(w);

            dx2 = 0;
            dy2 = h < 0 ? -1 : (h > 0 ? 1 : 0);
        }
        int numerator = fastStep >> 1;

        for (int i = 0; i <= fastStep; i++) {
            for (int xOffset = -1; xOffset <= 1; ++xOffset) {
                int x_ = x + xOffset;
                for (int yOffset = -1; yOffset <= 1; ++yOffset) {
                    int y_ = y + yOffset;
                    if (x_ < 0 || x_ > 99 || y_ < 0 || y_ > 99)
                        continue;
                    if (grids[x_][y_] == OBSTACLE)
                        return true;
                }
            }
            numerator += slowStep;
            if (numerator >= fastStep) {
                numerator -= fastStep;
                x += dx1;
                y += dy1;
            } else {
                x += dx2;
                y += dy2;
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
    public List<Vector2> findPath(Vector2 from, Vector2 to, boolean strict) {
        int[] start = toGridPos(from);
        int[] end = toGridPos(to);

        Map<GameMapNode, GameMapNode> visited = new HashMap<>();
        PriorityQueue<GameMapNode> visiting = new PriorityQueue<>();
        GameMapNode beginNode = GameMapNode.makeStartNode(start, end);
        visiting.add(beginNode);
        visited.put(beginNode, beginNode);

        GameMapNode endNode = null;

        boolean foundEnd = false;
        while (!foundEnd && visiting.size() > 0) {
            // choose best node
            GameMapNode choice = visiting.poll();

            // 8 dir expand
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    if (i == 0 && j == 0)
                        continue;
                    GameMapNode move = choice.makeMove(strict ? map2 : map1, i, j, end);
                    if (move != null) {
                        if (move.x == end[0] && move.y == end[1]) {
                            foundEnd = true;
                            endNode = move;
                            break;
                        }
                        if (!visited.containsKey(move)) {
                            // new visiting
                            visited.put(move, move);
                            visiting.add(move);
                        } else {
                            // update the G value
                            GameMapNode theMove = visited.get(move);
                            theMove.tryUpdateByPrevious(strict ? map2 : map1, choice);
                        }
                    }
                }
                if (foundEnd)
                    break;
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
        // while (true) {
        // GameMapNode next = it.next();
        // if (!it.hasNext())
        // break;
        // // if (current.x == next.x || current.y == next.y) {
        // if (!hasObstacle(current, next)) {
        // // 移除多余的顶点
        // it.remove();
        // } else {
        // // 从新的顶点开始计算
        // current = next;
        // }
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
