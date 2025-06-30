package io.agora.recording.example;

import io.agora.recording.AgoraMediaComponentFactory;
import io.agora.recording.AgoraParameter;
import io.agora.recording.AgoraService;
import io.agora.recording.AgoraServiceConfiguration;
import io.agora.recording.LogConfig;
import io.agora.recording.example.utils.SampleLogger;

public class AgoraServiceInitializer {
    private static AgoraService agoraService;
    private static AgoraMediaComponentFactory factory;

    public static void initService(RecorderConfig recorderConfig) {
        SampleLogger.info("AgoraService initService SDK Version: " + AgoraService.getSdkVersion());
        if (recorderConfig == null) {
            SampleLogger.info("recorderConfig is null");
            return;
        }
        if (agoraService == null) {
            agoraService = new AgoraService();

            AgoraServiceConfiguration config = new AgoraServiceConfiguration();
            config.setEnableAudioDevice(false);
            config.setEnableAudioProcessor(true);
            config.setEnableVideo(true);
            config.setAppId(recorderConfig.getAppId());
            config.setUseStringUid(recorderConfig.isUseStringUid());
            LogConfig logConfig = new LogConfig();
            logConfig.setFileSizeInKB(1024 * 1024 * 5);
            logConfig.setFilePath("logs/agora_logs/agorasdk.log");
            config.setLogConfig(logConfig);
            int ret = agoraService.initialize(config);
            if (ret != 0) {
                SampleLogger.info("Failed to initialize AgoraService, error: " + ret);
                return;
            }
        }

        AgoraParameter parameter = agoraService.getAgoraParameter();
        if (recorderConfig.isUseCloudProxy()) {    
            if (parameter != null) {
                parameter.setBool("rtc.enable_proxy", true);
                SampleLogger.info("set the Cloud_Proxy Open!");
            }
        }
        if (recorderConfig.isRecoverFile()) {
            parameter.setBool("che.media_recorder_recover_files", true);
        }

        if (factory == null) {
            factory = agoraService.createAgoraMediaComponentFactory();
            if (null == factory) {
                SampleLogger.info("Failed to create createAgoraMediaComponentFactory");
                return;
            }
        }

        SampleLogger.info("AgoraService initialized");
    }

    public static void destroy() {
        SampleLogger.info("destroy");
        if (null == agoraService || null == factory) {
            SampleLogger.info("destroy agoraService is null");
            return;
        }
        factory.release();
        agoraService.release();

        factory = null;
        agoraService = null;
    }

    public static AgoraService getAgoraService() {
        return agoraService;
    }

    public static AgoraMediaComponentFactory getFactory() {
        return factory;
    }
}
