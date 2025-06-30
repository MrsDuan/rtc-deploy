package io.agora.recording.example;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import io.agora.recording.example.utils.SampleLogger;
import io.agora.recording.example.utils.Utils;

public class RecordingApplication {
    private static final CountDownLatch leaveLatch = new CountDownLatch(1);
    private static final RecordingManager recordingManager = new RecordingManager();

    public static void main(String[] args) {
        try {
            RecorderConfigManager.parseArgs(args);
            SampleLogger.info("Recording config: " + RecorderConfigManager.getConfig());

            AgoraServiceInitializer.initService(RecorderConfigManager.getConfig());

            String taskId = Utils.getTaskId();
            recordingManager.startRecording(taskId, RecorderConfigManager.getConfig());

            // Start a new thread to listen for console input
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();
                    if ("1".equals(input)) {
                        leaveLatch.countDown();
                        break;
                    } else if ("9".equals(input)) {
                        SampleLogger.info("程序即将终止");
                        System.exit(1);
                    }
                }
                scanner.close();
            }).start();

            leaveLatch.await();

            recordingManager.stopRecording(taskId, false);

        } catch (Exception e) {
            SampleLogger.error("Recording failed e:" + e);
        } finally {
            recordingManager.destroy();
            AgoraServiceInitializer.destroy();

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                SampleLogger.error("exit failed");
            }
            SampleLogger.release();
            System.exit(0);
        }
    }
}
