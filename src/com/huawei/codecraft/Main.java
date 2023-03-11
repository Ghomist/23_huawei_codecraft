package com.huawei.codecraft;

import com.huawei.codecraft.controller.GameController;

public class Main {
    public static void main(String[] args) {
        GameController controller = new GameController();
        controller.init();
        controller.run();
    }

    private static void schedule() {
        // int frameID;
        // while (inStream.hasNextLine()) {
        // String line = inStream.nextLine();
        // String[] parts = line.split(" ");
        // frameID = Integer.parseInt(parts[0]);
        // readUtilOK();

        // outStream.printf("%d\n", frameID);
        // int lineSpeed = 3;
        // double angleSpeed = 1.5;
        // for (int robotId = 0; robotId < 4; robotId++) {
        // outStream.printf("forward %d %d\n", robotId, lineSpeed);
        // outStream.printf("rotate %d %f\n", robotId, angleSpeed);
        // }
        // OK();
        // }
    }
}
