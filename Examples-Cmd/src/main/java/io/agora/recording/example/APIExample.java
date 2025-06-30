package io.agora.recording.example;

import io.agora.recording.AgoraMediaComponentFactory;
import io.agora.recording.AgoraMediaRtcRecorder;
import io.agora.recording.AgoraParameter;
import io.agora.recording.AgoraService;
import io.agora.recording.AgoraServiceConfiguration;
import io.agora.recording.Constants.VideoStreamType;
import io.agora.recording.IAgoraMediaRtcRecorderEventHandler;
import io.agora.recording.MediaRecorderConfiguration;
import io.agora.recording.VideoMixingLayout;
import io.agora.recording.VideoSubscriptionOptions;
import io.agora.recording.WatermarkConfig;
import io.agora.recording.example.utils.SampleLogger;

public class APIExample implements IAgoraMediaRtcRecorderEventHandler {
    private static AgoraMediaRtcRecorder agoraMediaRtcRecorder;
    private static boolean isMix = true;

    public static class AgoraMediaRtcRecorderEventHandler implements IAgoraMediaRtcRecorderEventHandler {
        @Override
        public void onFirstRemoteAudioDecoded(String channelId, String userId, int elapsed) {
            new Thread() {
                @Override
                public void run() {
                    MediaRecorderConfiguration mediaRecorderConfiguration = new MediaRecorderConfiguration();
                    agoraMediaRtcRecorder.setRecorderConfigByUid(mediaRecorderConfiguration, userId);

                    agoraMediaRtcRecorder.startSingleRecordingByUid(userId);
                }
            }.start();
        }

        @Override
        public void onFirstRemoteVideoDecoded(String channelId, String userId, int width, int height, int elapsed) {
            new Thread() {
                @Override
                public void run() {
                    if (isMix) {
                        VideoMixingLayout layout = new VideoMixingLayout();
                        agoraMediaRtcRecorder.setVideoMixingLayout(layout);
                    } else {
                        MediaRecorderConfiguration mediaRecorderConfiguration = new MediaRecorderConfiguration();
                        agoraMediaRtcRecorder.setRecorderConfigByUid(mediaRecorderConfiguration, userId);

                        agoraMediaRtcRecorder.startSingleRecordingByUid(userId);
                    }
                }
            }.start();
        }
    }

    public static void main(String[] args) {
        AgoraService agoraService = new AgoraService();

        AgoraServiceConfiguration config = new AgoraServiceConfiguration();
        config.setEnableAudioDevice(false);
        config.setEnableAudioProcessor(true);
        config.setEnableVideo(true);
        config.setAppId("APPID");
        config.setUseStringUid(false);
        agoraService.initialize(config);

        AgoraParameter parameter = agoraService.getAgoraParameter();
        if (parameter != null) {
            parameter.setBool("rtc.enable_proxy", true);
            SampleLogger.info("set the Cloud_Proxy Open!");
        }

        AgoraMediaComponentFactory factory = agoraService.createAgoraMediaComponentFactory();

        agoraMediaRtcRecorder = factory.createMediaRtcRecorder();
        agoraMediaRtcRecorder.initialize(agoraService, false);
        AgoraMediaRtcRecorderEventHandler handler = new AgoraMediaRtcRecorderEventHandler();
        agoraMediaRtcRecorder.registerRecorderEventHandler(handler);

        agoraMediaRtcRecorder.joinChannel("token",
                "channelName",
                "0");

        agoraMediaRtcRecorder.subscribeAllAudio();
        VideoSubscriptionOptions options = new VideoSubscriptionOptions();
        options.setEncodedFrameOnly(false);
        options.setType(VideoStreamType.VIDEO_STREAM_HIGH);
        agoraMediaRtcRecorder.subscribeAllVideo(options);

        // set recorder config
        MediaRecorderConfiguration mediaRecorderConfiguration = new MediaRecorderConfiguration();
        agoraMediaRtcRecorder.setRecorderConfig(mediaRecorderConfiguration);

        // set watermark
        WatermarkConfig[] watermarks = new WatermarkConfig[1];
        agoraMediaRtcRecorder.enableAndUpdateVideoWatermarks(watermarks);

        agoraMediaRtcRecorder.startRecording();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        agoraMediaRtcRecorder.unsubscribeAllAudio();
        agoraMediaRtcRecorder.unsubscribeAllVideo();

        if (isMix) {
            agoraMediaRtcRecorder.stopRecording();
        } else {
            agoraMediaRtcRecorder.stopSingleRecordingByUid("userId");
        }

        agoraMediaRtcRecorder.unregisterRecorderEventHandle(handler);

        agoraMediaRtcRecorder.leaveChannel();
        agoraMediaRtcRecorder.release();

        agoraService.release();
    }
}
