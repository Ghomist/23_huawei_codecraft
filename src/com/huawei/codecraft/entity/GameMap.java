package com.huawei.codecraft.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import com.huawei.codecraft.helper.ArrayHelper;
import com.huawei.codecraft.io.Output;
import com.huawei.codecraft.math.Vector2;
import com.huawei.codecraft.math.Vector2Int;
import com.huawei.codecraft.util.BitCalculator;

public class GameMap {

    public static final int OBSTACLE = -1;
    public static final int EMPTY = 0;

    public static final int B1 = 0b0000000010;
    public static final int B2 = 0b0000000100;
    public static final int B3 = 0b0000001000;
    public static final int B4 = 0b0000010000;
    public static final int B5 = 0b0000100000;
    public static final int B6 = 0b0001000000;
    public static final int B7 = 0b0010000000;
    public static final int B8 = 0b0100000000;
    public static final int B9 = 0b1000000000;

    public static final int B123 = 0b0000001110;
    public static final int B456 = 0b0001110000;
    public static final int B1234567 = 0b0011111110;

    private static final double PATH_ADJUST = 0.25;

    private int[][] grid = new int[100][100];
    private double[][][] dist; // 距离场
    private double[][][] distStrict; // 严格模式的距离场
    private final Vector2[] obstacles;
    private final Workbench[] benches;
    private List<Scheme> schemes = new ArrayList<>();

    public GameMap(int[][] grid, Workbench[] benches) {
        this.benches = benches;
        List<Vector2> obs = new LinkedList<>();

        for (int x = 0; x < 100; ++x) {
            for (int y = 0; y < 100; ++y) {
                this.grid[x][y] = grid[99 - y][x];
                if (this.grid[x][y] == OBSTACLE) {
                    obs.add(new Vector2(x / 2.0 + 0.25, y / 2.0 + 0.25));
                }
            }
        }
        obstacles = obs.toArray(new Vector2[obs.size()]);

        dist = new double[benches.length][100][100];
        distStrict = new double[benches.length][100][100];
        LinkedList<Vector2Int> list = new LinkedList<>();
        Set<Vector2Int> visited = new HashSet<>();
        for (Workbench bench : benches) {
            int id = bench.id;
            for (double[] line : dist[id]) {
                Arrays.fill(line, Double.MAX_VALUE);
            }
            for (double[] line : distStrict[id]) {
                Arrays.fill(line, Double.MAX_VALUE);
            }
            // do bfs
            Vector2Int start = bench.getPos().toGrid();
            list.clear();
            list.add(start);
            visited.clear();
            visited.add(start);
            dist[id][start.x][start.y] = 0;
            while (list.size() > 0) {
                Vector2Int node = list.removeFirst();
                for (int i = -1; i <= 1; ++i) {
                    for (int j = -1; j <= 1; ++j) {
                        Vector2Int move = node.offset(i, j);
                        if (ArrayHelper.safeGet(this.grid, move.x, move.y, OBSTACLE) == OBSTACLE)
                            continue;
                        if (visited.add(move)) {
                            double moveDist = move.x == node.x || move.y == node.y ? 1 : 1.414;
                            dist[id][move.x][move.y] = dist[id][node.x][node.y] + moveDist;
                            list.addLast(move);
                        }
                    }
                }
            }
            // do strict bfs
            start = bench.getPos().toGrid();
            if (nearByWall(start))
                continue;
            list.clear();
            list.add(start);
            visited.clear();
            visited.add(start);
            distStrict[id][start.x][start.y] = 0;
            while (list.size() > 0) {
                Vector2Int node = list.removeFirst();
                for (int i = -1; i <= 1; ++i) {
                    for (int j = -1; j <= 1; ++j) {
                        Vector2Int move = node.offset(i, j);
                        if (nearByWall(move))
                            continue;
                        if (visited.add(move)) {
                            double moveDist = move.x == node.x || move.y == node.y ? 1 : 1.414;
                            distStrict[id][move.x][move.y] = distStrict[id][node.x][node.y] + moveDist;
                            list.addLast(move);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < benches.length; i++) {
            for (int j = 0; j < benches.length; j++) {
                if (i == j)
                    continue;
                if (Scheme.isAvailableScheme(this, benches[i], benches[j])) {
                    schemes.add(new Scheme(this, benches[i], benches[j]));
                }
            }
        }
        // for (Workbench a : benches) {
        // for (Workbench b : benches) {
        // if (Scheme.isAvailableScheme(this, a, b)) {
        // schemes.add(new Scheme(this, a, b));
        // }
        // }
        // }
    }

    public Vector2[] getObstacles() {
        return obstacles;
    }

    public List<Scheme> getSchemes() {
        return schemes;
    }

    /**
     * 根据距离场，极速获取任意一点到工作台的距离（寻路距离）
     * 
     * @param pos     任意点的坐标
     * @param benchID 工作台的 id
     * @param strict  持物品时应该使用严格模式
     * @return 寻路格点距离
     */
    public double getDistToWorkbench(Vector2 pos, int benchID, boolean strict) {
        Vector2Int grid = pos.toGrid();
        return strict ? distStrict[benchID][grid.x][grid.y] : dist[benchID][grid.x][grid.y];
    }

    /**
     * 获取最近的工作台
     * 
     * @param pos  出发点位置
     * @param type 需要的工作台类型（注意是用二进制位来表示的）
     *             比如{@code GameMap.B123}实际值是{@code 0b1110}，表示需要第1、2、3号的工作台，示例：
     *             {@code getClosestWorkbench(robot.getPos(), GameMap.B123 | GameMap.B456)}
     * @param sell 是否是“卖出物品”
     * @return 工作台的 id（找不到任何工作台时会返回{@code -1}）
     */
    public int getClosestWorkbench(Vector2 pos, int type, boolean sell) {
        Vector2Int p = pos.toGrid();
        double minDist = Double.MAX_VALUE;
        int minID = 0;
        for (int id = 0; id < dist.length; ++id) {
            Workbench b = benches[id];
            if (BitCalculator.isOne(type, b.getType())) {
                double d = sell ? distStrict[id][p.x][p.y] : dist[id][p.x][p.y];
                if (d < minDist) {
                    minDist = d;
                    minID = id;
                }
            }
        }
        return minDist == Double.MAX_VALUE ? -1 : minID;
    }

    /**
     * 坐标是否靠墙
     * 
     * @param pos 坐标
     */
    public boolean nearByWall(Vector2Int pos) {
        return nearByWall(pos.x, pos.y);
    }

    /**
     * 坐标是否靠撞墙
     * 
     * @param x 横坐标
     * @param y 纵坐标
     */
    public boolean nearByWall(int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (ArrayHelper.safeGet(grid, x + i, y + j, OBSTACLE) == OBSTACLE) {
                    return true;
                }
            }
        }
        return false;
    }

    public int nearByWallCount(int x, int y) {
        int cnt = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (ArrayHelper.safeGet(grid, x + i, y + j, OBSTACLE) == OBSTACLE)
                    cnt++;
            }
        }
        return cnt;
    }

    /**
     * 寻找网格上的两点的直线是否存在障碍物
     * 
     * @return {@code true} 如果存在障碍物
     */
    @Deprecated
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
                    if (ArrayHelper.safeGet(grid, x_, y_, OBSTACLE) == OBSTACLE)
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

    public LinkedList<Vector2Int> rawPath = null;

    /**
     * 正常地图的 A* 寻路
     * 
     * @param from   起始点坐标
     * @param to     终点坐标
     * @param strict 机器人拿东西的时候寻路应使用严格模式
     */
    public List<Vector2> findPath(Robot[] robots, int id, Vector2 from, Vector2 to, boolean strict) {
        Vector2Int start = from.toGrid();
        Vector2Int end = to.toGrid();
        if (start.x == end.x && start.y == end.y) {
            List<Vector2> ret = new LinkedList<>();
            ret.add(to);
            return ret;
        }

        double[][] g = new double[100][100];
        Vector2Int[][] last = new Vector2Int[100][100];
        PriorityQueue<Vector2Int> nodes = new PriorityQueue<>((a, b) -> {
            return Double.compare(g[a.x][a.y] + menhadenDist(a.x, a.y, end.x, end.y),
                    g[b.x][b.y] + menhadenDist(b.x, b.y, end.x, end.y));
        });
        // PriorityQueue<Vector2Int> nodes = new PriorityQueue<>((a, b) -> {
        // return Double.compare(g[a.x][a.y] + ojldDist(a.x, a.y, end.x, end.y),
        // g[b.x][b.y] + ojldDist(b.x, b.y, end.x, end.y));
        // });

        Vector2Int begin = start;
        nodes.add(begin);
        g[begin.x][begin.y] = 0.001;

        Vector2Int endPos = null;
        while (nodes.size() > 0 && endPos == null) {
            Vector2Int choose = nodes.poll();
            int x = choose.x;
            int y = choose.y;
            for (int i = -1; i <= 1 && endPos == null; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0)
                        continue;
                    if (i != 0 && j != 0) // 四向寻路
                        continue;
                    int x_ = x + i;
                    int y_ = y + j;
                    if (strict) {
                        if (nearByWall(x_, y_))
                            continue;
                    } else {
                        if (ArrayHelper.safeGet(grid, x_, y_, OBSTACLE) == OBSTACLE
                                || nearByWallCount(x_, y_) >= 3) {
                            continue;
                        }
                    }
                    boolean stuck = false;
                    for (Robot r : robots) {
                        if (r.id == id)
                            continue;
                        if (Math.abs(r.getPos().x - (x_ / 2.0 + 0.25)) < 2
                                && Math.abs(r.getPos().y - (y_ / 2.0 + 0.25)) < 2) {
                            stuck = true;
                            break;
                        }
                    }
                    if (stuck)
                        continue;
                    if (x_ < 0 || x_ >= 100 || y_ < 0 || y_ >= 100)
                        continue;
                    if (x_ == end.x && y_ == end.y) {
                        // arrive end
                        endPos = new Vector2Int(x_, y_);
                        last[x_][y_] = choose;
                        break;
                    }
                    if (g[x_][y_] == 0) {
                        // new visit
                        g[x_][y_] = g[x][y] + 1;
                        last[x_][y_] = choose;
                        nodes.add(new Vector2Int(x_, y_));
                    } else {
                        // visited, update
                        double newG = g[x][y] + 1;
                        if (newG <= g[x_][y_]) {
                            g[x_][y_] = newG;
                            last[x_][y_] = choose;
                        }
                    }
                }
            }
        }

        if (endPos != null) {
            LinkedList<Vector2Int> list = new LinkedList<>();

            while (last[endPos.x][endPos.y] != null) {
                list.addFirst(endPos);
                endPos = last[endPos.x][endPos.y];
            }
            list.addFirst(endPos);
            if (obstacles.length == 2485)
                list.removeFirst();

            List<Vector2> ret = smoothPath(list);
            rawPath = list;
            return ret;
        } else {
            // no path
            rawPath = null;
            return null;
        }

        // Map<GameMapNode, GameMapNode> visited = new HashMap<>();
        // PriorityQueue<GameMapNode> visiting = new PriorityQueue<>();
        // GameMapNode beginNode = GameMapNode.makeStartNode(start, end);
        // visiting.add(beginNode);
        // visited.put(beginNode, beginNode);

        // GameMapNode endNode = null;

        // boolean foundEnd = false;
        // while (!foundEnd && visiting.size() > 0) {
        // // choose best node
        // GameMapNode choice = visiting.poll();

        // // 8 dir expand
        // for (int i = -1; i <= 1; ++i) {
        // for (int j = -1; j <= 1; ++j) {
        // if (i == 0 && j == 0)
        // continue;
        // if (i != 0 && j != 0) // 四向寻路
        // continue;
        // GameMapNode move = choice.makeMove(grid, i, j, end, strict);
        // if (move != null) {
        // if (move.x == end.x && move.y == end.y) {
        // foundEnd = true;
        // endNode = move;
        // break;
        // }
        // if (!visited.containsKey(move)) {
        // // new visiting
        // visited.put(move, move);
        // visiting.add(move);
        // } else {
        // // update the G value
        // GameMapNode theMove = visited.get(move);
        // theMove.tryUpdateByPrevious(grid, choice);
        // }
        // }
        // }
        // if (foundEnd)
        // break;
        // }
        // }

        // if (endNode != null) {
        // LinkedList<GameMapNode> ans = new LinkedList<>();

        // ans.add(endNode);
        // while (endNode.pre != null) {
        // ans.addFirst(endNode.pre);
        // endNode = endNode.pre;
        // }

        // return smoothPath(ans);
        // } else {
        // // no path
        // return null;
        // }
    }

    private List<Vector2> smoothPath(List<Vector2Int> path) {
        // 消除长直线
        Vector2Int[] pathArr = path.toArray(new Vector2Int[path.size()]);
        path.clear();
        path.add(pathArr[0]);
        for (int i = 1; i < pathArr.length - 1; ++i) {
            Vector2Int last = pathArr[i - 1];
            Vector2Int crt = pathArr[i];
            Vector2Int nxt = pathArr[i + 1];
            boolean canRemove = last.x == crt.x && nxt.x == crt.x || last.y == crt.y && nxt.y == crt.y;
            if (!canRemove) {
                path.add(crt);
            }
        }
        path.add(pathArr[pathArr.length - 1]);

        // 消除拐角
        pathArr = path.toArray(new Vector2Int[path.size()]);
        path.clear();
        path.add(pathArr[0]);
        int start = 0;
        for (int i = 1; i < pathArr.length - 1; ++i) {
            Vector2Int crt = pathArr[i];
            Vector2Int nxt = pathArr[i + 1];
            boolean canRemove = !hasObstacle(pathArr[start], nxt);
            if (!canRemove) {
                path.add(crt);
                start = i;
            }
        }
        path.add(pathArr[pathArr.length - 1]);

        return path.stream()
                .map(p -> {
                    if (ArrayHelper.safeGet(grid, p.x + 1, p.y, OBSTACLE) == OBSTACLE) {
                        return p.toPos().add(new Vector2(-PATH_ADJUST, 0));
                    } else if (ArrayHelper.safeGet(grid, p.x - 1, p.y, OBSTACLE) == OBSTACLE) {
                        return p.toPos().add(new Vector2(PATH_ADJUST, 0));
                    } else if (ArrayHelper.safeGet(grid, p.x, p.y + 1, OBSTACLE) == OBSTACLE) {
                        return p.toPos().add(new Vector2(0, -PATH_ADJUST));
                    } else if (ArrayHelper.safeGet(grid, p.x, p.y - 1, OBSTACLE) == OBSTACLE) {
                        return p.toPos().add(new Vector2(0, PATH_ADJUST));
                    } else {
                        return p.toPos();
                    }
                })
                .collect(Collectors.toList());

        // return path.stream()
        // .map(x -> x.getPos())
        // .collect(Collectors.toList());
    }

    private boolean hasObstacle(Vector2Int node1, Vector2Int node2) {
        // TODO: 优化
        int dx = node1.x < node2.x ? 1 : -1;
        int dy = node1.y < node2.y ? 1 : -1;
        for (int x = node1.x; x != node2.x + dx; x += dx) {
            for (int y = node1.y; y != node2.y + dy; y += dy) {
                if (ArrayHelper.safeGet(grid, x, y, OBSTACLE) == OBSTACLE) {
                    return true;
                }
            }
        }
        return false;
        // return hasObstacle(node1.x, node1.y, node2.x, node2.y);
    }

    private double ojldDist(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private int menhadenDist(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}