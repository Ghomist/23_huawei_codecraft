package com.huawei.codecraft.entity;

import com.huawei.codecraft.math.Vector2;

public class Request {

    public final Workbench publisher;
    public final int needType;

    private enum RequestStatus {
        WAITING, PENDING, SENDING, FINISHED
    }

    private RequestStatus status;
    private Robot robot;

    public Request(Workbench publisher, int type) {
        this.publisher = publisher;
        needType = type;

        status = RequestStatus.WAITING;
    }

    public void pendingBy(Robot robot) {
        if (this.robot != null) {
            // cancel last robot
            this.robot.interruptRequest();
        }
        // set current robot
        this.robot = robot;
        status = RequestStatus.PENDING;
    }

    public void sendingBy(Robot robot) {
        if (this.robot != robot)
            return;
        status = RequestStatus.SENDING;
    }

    public void finishedBy(Robot robot) {
        if (this.robot != robot)
            return;
        status = RequestStatus.FINISHED;
    }

    public int getType() {
        return needType;
    }

    public int getPriority() {
        return publisher.getType() == 9 ? 0 : (needType - 1) / 3;
    }

    public double getCurrentPendingCost() {
        if (isPending()) {
            return Vector2.distance(robot.getPos(), robot.getTargetPos());
        } else {
            return Double.MAX_VALUE;
        }
    }

    public boolean isWaiting() {
        return status == RequestStatus.WAITING;
    }

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public boolean isSending() {
        return status == RequestStatus.SENDING;
    }

    public boolean isFinished() {
        return status == RequestStatus.FINISHED;
    }

    public boolean tryReboot() {
        if (publisher.getType() >= 8) {
            status = RequestStatus.WAITING;
            robot = null;
            return true;
        } else {
            return false;
        }
    }
}
