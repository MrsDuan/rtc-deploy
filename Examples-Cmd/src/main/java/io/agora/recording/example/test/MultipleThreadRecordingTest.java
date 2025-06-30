package io.agora.recording.example.test;

import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.recording.example.AgoraServiceInitializer;
import io.agora.recording.example.RecorderConfigManager;
import io.agora.recording.example.RecordingManager;
import io.agora.recording.example.utils.SampleLogger;

public class MultipleThreadRecordingTest {
    private static final RecordingManager recordingManager = new RecordingManager();
    private static long testStartTime;

    public static void main(String[] args) {
        try {

            RecorderConfigManager.parseArgs(args);
            SampleLogger.info("Recording config: " + RecorderConfigManager.getConfig());

            AgoraServiceInitializer.initService(RecorderConfigManager.getConfig());

            ThreadPoolExecutor testTaskExecutorService = new ThreadPoolExecutor(
                    0,
                    Integer.MAX_VALUE,
                    1L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>());
            AtomicBoolean isStopped = new AtomicBoolean(false);
            Object[] lock = new Object[RecorderConfigManager.getThreadNum()];
            for (int i = 0; i < RecorderConfigManager.getThreadNum(); i++) {
                lock[i] = new Object();
            }

            testTaskExecutorService.submit(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();
                    if ("1".equals(input)) {
                        isStopped.set(true);
                        for (int i = 0; i < RecorderConfigManager.getThreadNum(); i++) {
                            synchronized (lock[i]) {
                                lock[i].notifyAll();
                            }
                        }
                        break;
                    } else if ("9".equals(input)) {
                        SampleLogger.info("程序即将终止");
                        System.exit(1);
                    }
                }
                scanner.close();
            });

            testStartTime = System.currentTimeMillis();

            for (int i = 0; i < RecorderConfigManager.getThreadNum(); i++) {
                final int threadIndex = i;
                testTaskExecutorService.submit(() -> {
                    String channelName = RecorderConfigManager.getConfig().getChannelName();
                    if (RecorderConfigManager.getThreadNum() > 1) {
                        channelName = channelName + "_" + threadIndex;
                    }

                    while (checkTestTime(RecorderConfigManager.getTestTime()) && !isStopped.get()) {
                        SampleLogger.info("threadIndex:" + threadIndex + " checkTestTime:"
                                + checkTestTime(RecorderConfigManager.getTestTime()));
                        try {
                            String taskId = io.agora.recording.example.utils.Utils.getTaskId();
                            recordingManager.startRecording(taskId, RecorderConfigManager.getConfig(),
                                    channelName);
                            synchronized (lock[threadIndex]) {
                                lock[threadIndex].wait(RecorderConfigManager.getOneTestTime() * 1000);
                            }
                            recordingManager.stopRecording(taskId, false);
                            Thread.sleep(1 * 1000);
                        } catch (Exception e) {
                            SampleLogger
                                    .error("Thread interrupted while waiting for testTaskExecutorService to complete");
                        }
                    }
                });
            }

            while (testTaskExecutorService.getActiveCount() > 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    SampleLogger.error("Thread interrupted while waiting for testTaskExecutorService to complete");
                }
            }

            testTaskExecutorService.shutdown();
        } catch (Exception e) {
            SampleLogger.error("Recording failed");
        } finally {
            SampleLogger.info("test task finished");
            recordingManager.destroy();
            AgoraServiceInitializer.destroy();

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                SampleLogger.error("exit failed");
            }
            System.exit(0);
        }
    }

    private static boolean checkTestTime(int testTime) {
        long currentTime = System.currentTimeMillis();
        long testCostTime = currentTime - testStartTime;
        if (testCostTime >= testTime * 1000) {
            return false;
        }
        return true;
    }
}
