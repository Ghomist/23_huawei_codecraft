package com.huawei.codecraft.entity;

public class GameMap {

    private int[][] grids = new int[100][100];

    public void SetGridType(int x, int y, int type) {
        grids[x][y] = type;
    }
}
