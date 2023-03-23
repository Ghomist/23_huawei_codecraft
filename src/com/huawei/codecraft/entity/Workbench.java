package com.huawei.codecraft.entity;

import com.huawei.codecraft.controller.GameController;
import com.huawei.codecraft.math.Vector2;
import com.huawei.codecraft.util.BitCalculator;

public class Workbench {

    public int id;

    private int type;
    private Vector2 pos;
    private int remainFrames;
    private boolean hasProduction;
    private boolean onProducing;
    private int materialStatus;

    private boolean[] pendingMaterial = new boolean[10];
    private boolean isOrdered = false;

    public Workbench(int id, int type, Vector2 pos) {
        this.pos = pos;
        this.id = id;
        this.type = type;
    }

    public void update(String info) {
        String[] infos = info.split(" ");
        type = infos[0].charAt(0) - '0';

        double x = Double.parseDouble(infos[1]);
        double y = Double.parseDouble(infos[2]);
        pos = new Vector2(x, y);

        int remainFrames = Integer.parseInt(infos[3]);
        onProducing = this.remainFrames <= 0 && remainFrames > 0;
        if (onProducing) {
            onProducing();
        }
        this.remainFrames = remainFrames;
        materialStatus = Integer.parseInt(infos[4]);
        hasProduction = infos[5].charAt(0) == '1';
    }

    private void onProducing() {
        publishRequest();
    }

    public void publishRequest() {
        switch (type) {
            case 4:
                GameController.instance.publishRequest(new Request(this, 1));
                GameController.instance.publishRequest(new Request(this, 2));
                break;
            case 5:
                GameController.instance.publishRequest(new Request(this, 1));
                GameController.instance.publishRequest(new Request(this, 3));
                break;
            case 6:
                GameController.instance.publishRequest(new Request(this, 2));
                GameController.instance.publishRequest(new Request(this, 3));
                break;
            case 7:
                GameController.instance.publishRequest(new Request(this, 4));
                GameController.instance.publishRequest(new Request(this, 5));
                GameController.instance.publishRequest(new Request(this, 6));
                break;
            case 8:
                GameController.instance.publishRequest(new Request(this, 7));
                break;
            case 9:
                GameController.instance.publishRequest(new Request(this, 1));
                GameController.instance.publishRequest(new Request(this, 2));
                GameController.instance.publishRequest(new Request(this, 3));
                GameController.instance.publishRequest(new Request(this, 4));
                GameController.instance.publishRequest(new Request(this, 5));
                GameController.instance.publishRequest(new Request(this, 6));
                GameController.instance.publishRequest(new Request(this, 7));
                break;
        }
    }

    public boolean isDangerous() {
        return (pos.x <= 3 || pos.x >= 48 || pos.y <= 3 || pos.y >= 48);
    }

    public int getType() {
        return type;
    }

    public Vector2 getPos() {
        return pos;
    }

    public int getRemainFrames() {
        return remainFrames;
    }

    public boolean isProducing() {
        return remainFrames > 0;
    }

    public boolean isFree() {
        return remainFrames == -1;
    }

    public boolean hasFinished() {
        return remainFrames == 0;
    }

    public boolean isProducingOrFinish() {
        return remainFrames >= 0;
    }

    public boolean hasMaterial(int item) {
        return BitCalculator.isOne(materialStatus, item) || pendingMaterial[item];
    }

    public boolean hasProduction() {
        return hasProduction;
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public void setOrder(boolean order) {
        isOrdered = order;
    }

    public void setPendingMaterial(int material) {
        pendingMaterial[material] = true;
    }

    public void finishPendingMaterial(int material) {
        pendingMaterial[material] = false;
    }
}
