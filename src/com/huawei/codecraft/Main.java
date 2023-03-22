package com.huawei.codecraft;

import java.io.IOException;

import com.huawei.codecraft.controller.GameController;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 如果在本地调试时不需要重启，在启动参数中添加restart，如：java -jar main.jar restart
        if (args.length <= 0) {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("java", "-jar", "-Xmn512m", "-Xms1024m", "-Xmx1024m",
                    "-XX:TieredStopAtLevel=1", "main.jar", "restart");
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
        } else if (!args[0].equals("restart")) {
            System.out.println("err");
        } else {
            GameController controller = new GameController();
            controller.init();
            controller.run();
        }
    }

    // private static void schedule() {
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
    // }
}
