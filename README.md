# Agora Recording Java SDK

[中文](./README.zh.md) | English

## Table of Contents

1.  [Introduction](#introduction)
2.  [Development Environment Requirements](#development-environment-requirements)
    - [Hardware Environment](#hardware-environment)
    - [Network Requirements](#network-requirements)
    - [Bandwidth Requirements](#bandwidth-requirements)
    - [Software Environment](#software-environment)
3.  [SDK Download](#sdk-download)
4.  [Integrating the SDK](#integrating-the-sdk)
    - [1. Maven Integration](#1-maven-integration)
    - [2. Local SDK Integration](#2-local-sdk-integration)
    - [3. Loading Native Libraries (.so files)](#3-loading-native-libraries-so-files)
      - [3.1 Extracting .so Files](#31-extracting-so-files)
      - [3.2 Configuring Load Paths](#32-configuring-load-paths)
5.  [Quick Start](#quick-start)
    - [Enable Service](#enable-service)
    - [Recording via Command Line](#recording-via-command-line)
      - [Prerequisites](#prerequisites)
      - [Compile Example Project](#compile-example-project)
      - [Configure Recording Parameters](#configure-recording-parameters)
      - [Start Recording](#start-recording)
      - [Stop Recording](#stop-recording)
    - [Recording via API Call](#recording-via-api-call)
      - [Prerequisites](#prerequisites-1)
      - [Implementing Recording via API Call](#implementing-recording-via-api-call)
        - [Initialize Service](#initialize-service)
        - [Join Channel](#join-channel)
        - [Configure and Start Recording](#configure-and-start-recording)
        - [Stop Recording](#stop-recording-1)
    - [Run the Maven Example Project](#run-the-maven-example-project)
6.  [API Reference](#api-reference)
7.  [Changelog](#changelog)
8.  [Other References](#other-references)

## Introduction

The Agora Recording Java SDK (v4.4.150.4) provides powerful real-time audio and video recording capabilities that can be seamlessly integrated into Java applications on Linux servers. With this SDK, your server can join an Agora channel as a dummy client to pull, subscribe to, and record audio and video streams within the channel in real-time. The recorded files can be used for content archiving, moderation, analysis, or other business-related advanced features.

## Development Environment Requirements

### Hardware Environment

- **Operating System**: Ubuntu 14.04+ or CentOS 6.5+ (7.0 recommended)
- **CPU Architecture**: x86-64, arm64

### Network Requirements

- **Public IP Address**
- **Domain Access**: Allow access to `.agora.io` and `.agoralab.co`

### Bandwidth Requirements

The required bandwidth depends on the number of channels to be recorded simultaneously and the conditions within those channels. The following data serves as a reference:

- Recording a 640 × 480 resolution stream requires approximately 500 Kbps.
- Recording a channel with two participants requires approximately 1 Mbps.
- Recording 100 such channels simultaneously requires approximately 100 Mbps.

### Software Environment

- **Build Tool**: Apache Maven or other build tools
- **JDK**: JDK 8+

## SDK Download

### Maven Download

#### x86_64 Platform

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150.4</version>
</dependency>
```

#### arm64 Platform

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150-aarch64</version>
</dependency>
```

### CDN Download

#### x86_64 Platform

[Agora-Linux-Recording-Java-SDK-v4.4.150.4-x86_64-738709-c4b18ea837-20250611_162648](https://download.agora.io/sdk/release/Agora-Linux-Recording-Java-SDK-v4.4.150.4-x86_64-738709-c4b18ea837-20250611_162648.zip)

#### arm64 Platform

[Agora-Linux-Recording-Java-SDK-v4.4.150-aarch64-565361-c502888569-20250213_112934](https://download.agora.io/sdk/release/Agora-Linux-Recording-Java-SDK-v4.4.150-aarch64-565361-c502888569-20250213_112934.jar)

## Integrating the SDK

There are two ways to integrate the SDK: via Maven integration or local SDK integration.

### 1. Maven Integration

Maven integration is the simplest way, automatically managing Java dependencies.

#### 1.1 Add Maven Dependency

Add the following dependency to your project's `pom.xml` file:

```xml
<!-- x86_64 Platform -->
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150.4</version>
</dependency>

<!-- arm64 Platform -->
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150-aarch64</version>
</dependency>
```

#### 1.2 Integrate .so Library Files

The Maven dependency includes the required JAR file, but you still need to manually handle the `.so` library files to run the application. Please refer to the **Loading Native Libraries (.so files)** section below.

### 2. Local SDK Integration

The local SDK is a complete package containing all necessary files, suitable for scenarios requiring more flexible control.

#### 2.1 SDK Package Structure

The SDK package (zip format) downloaded from the official website contains the following:

- **doc/** - JavaDoc documentation, detailed API descriptions
- **examples/** - Example code and projects
- **sdk/** - Core SDK files
  - `agora-recording-sdk.jar` - Java library
  - `agora-recording-sdk-javadoc.jar` - JavaDoc documentation

#### 2.2 Integrate JAR File

You can integrate the JAR file in two ways:

###### Local Maven Repository Method

Method 1: Install only the SDK JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-recording-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-recording-java-sdk \
  -Dversion=4.4.150.4 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

Method 2: Install both SDK JAR and JavaDoc JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-recording-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-recording-java-sdk \
  -Dversion=4.4.150.4 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -Djavadoc=sdk/agora-recording-sdk-javadoc.jar
```

After installation, add the dependency in `pom.xml`:

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150.4</version>
</dependency>
```

###### Direct Reference Method

1.  Copy the JAR files to your project's `libs` directory:

    ```sh
    mkdir -p libs
    cp sdk/agora-recording-sdk.jar libs/
    cp sdk/agora-recording-sdk-javadoc.jar libs/  # Optional, for IDE support
    ```

2.  Add the classpath reference in your Java project:

    ```sh
    # Use the SDK JAR
    java -cp .:libs/agora-recording-sdk.jar YourMainClass

    # Configure JavaDoc in your IDE (common IDEs like IntelliJ IDEA or Eclipse support direct association of JavaDoc JARs)
    ```

#### 2.3 Integrate .so Library Files

The downloaded SDK package already includes the `.so` files. You need to ensure that the Java program can find these files at runtime. Please refer to the **Loading Native Libraries (.so files)** section below.

### 3. Loading Native Libraries (.so files)

The Agora Linux Recording Java SDK depends on underlying C++ native libraries (`.so` files). Whether integrating via Maven or locally, you need to ensure the Java Virtual Machine (JVM) can find and load these libraries at runtime.

#### 3.1 Extracting .so Files

The `.so` files are contained within the `agora-recording-sdk.jar` or `linux-recording-java-sdk-x.x.x.x.jar` file. You need to extract them first:

1.  Create a directory in your project or deployment location to store the library files, for example, `libs`:

    ```sh
    mkdir -p libs
    cd libs
    ```

2.  Use the `jar` command to extract the contents from the SDK's JAR file (assuming the JAR file is in the `libs` directory or Maven cache):

    ```sh
    # If using local integration, the JAR file is usually in the libs directory
    jar xvf agora-recording-sdk.jar

    # If using Maven integration, the JAR file is in the Maven cache, e.g.:
    # jar xvf ~/.m2/repository/io/agora/rtc/linux-recording-java-sdk/4.4.150.4/linux-recording-java-sdk-4.4.150.4.jar
    ```

3.  After extraction, a `native/linux/x86_64` subdirectory (or `aarch64` for ARM) will be generated in the `libs` directory, containing the required `.so` files:

    ```
    libs/
    ├── agora-recording-sdk.jar (or empty, if only used for extraction)
    ├── io/          # Java class files location, no need to worry about
    ├── META-INF/    # JAR file and application-related metadata, no need to worry about
    └── native/      # Native library files for the corresponding platform
        └── linux/
            └── x86_64/   # x86_64 platform .so libraries
                ├── libagora_rtc_sdk.so
                ├── libagora-fdkaac.so
                ├── libaosl.so
                └── librecording.so
            └── aarch64/  # arm64 platform .so libraries (if available)
    ```

#### 3.2 Configuring Load Paths

There are two main methods to let the JVM find the `.so` files:

**Method 1: Setting the `LD_LIBRARY_PATH` Environment Variable (Recommended)**

This is the most reliable way, especially when there are dependencies between `.so` files.

```sh
# Determine the directory containing your .so files, assuming ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# Set the LD_LIBRARY_PATH environment variable, adding the library directory to the front of the existing path
export LD_LIBRARY_PATH=$LIB_DIR:$LD_LIBRARY_PATH

# Run your Java application
java -jar YourApp.jar
# Or using classpath
# java -cp "YourClasspath" YourMainClass
```

**Method 2: Using the JVM Parameter `-Djava.library.path`**

This method directly tells the JVM where to look for library files.

```sh
# Determine the directory containing your .so files, assuming ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# Run the Java application, specifying the library path via the -D parameter
java -Djava.library.path=$LIB_DIR -jar YourApp.jar
# Or using classpath
# java -Djava.library.path=$LIB_DIR -cp "YourClasspath" YourMainClass
```

> **Note**:
>
> - Method 1 (`LD_LIBRARY_PATH`) is recommended because it handles dependencies between libraries better. If you only use `-Djava.library.path`, loading might fail sometimes because a library cannot find other libraries it depends on.
> - Ensure `$LIB_DIR` points to the **exact directory** containing files like `libagora_rtc_sdk.so`.
> - You can place the command to set the environment variable in a startup script so it's configured automatically each time the application runs.

Refer to the following script example, which combines both methods and sets the classpath:

```sh
#!/bin/bash
# Get the absolute path of the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Determine the .so library file path (assuming in libs/native/linux/x86_64 relative to the script)
# Adjust x86_64 to your target architecture (e.g., aarch64) if needed
LIB_PATH="$SCRIPT_DIR/libs/native/linux/x86_64"
# SDK JAR path (assuming in libs relative to the script)
SDK_JAR="$SCRIPT_DIR/libs/agora-recording-sdk.jar"
# Your application's main class
MAIN_CLASS="YourMainClass"
# Your application's other dependency classpath (if any)
APP_CP="YourOtherClasspath"

# Check if the library directory exists
if [ ! -d "$LIB_PATH" ]; then
  echo "Error: Library directory not found: $LIB_PATH" >&2
  exit 1
fi

# Set the library path environment variable
export LD_LIBRARY_PATH=$LIB_PATH:$LD_LIBRARY_PATH

# Combine the classpath
CLASSPATH=".:$SDK_JAR:$APP_CP" # '.' represents the current directory

# Execute the Java program
# Use both LD_LIBRARY_PATH and -Djava.library.path for compatibility
java -Djava.library.path=$LIB_PATH -cp "$CLASSPATH" $MAIN_CLASS
```

## Quick Start

### Enable Service

Refer to [Enable Service on the official website](https://docs.agora.io/en/recording/java/get-started/enable-service) (Link might need update based on documentation structure).

### Recording via Command Line

#### Prerequisites

Before starting, ensure you have completed the environment preparation and SDK integration steps.

> **Note**: When the recording SDK joins a channel, it acts as a dummy client. Therefore, it needs to join the same channel using the same App ID and channel profile as the Agora RTC SDK clients.

#### Compile Example Project

Execute the build script in the `Examples-Cmd` directory:

```sh
cd Examples-Cmd
./build.sh
```

#### Configure Recording Parameters

Recording parameters are configured using JSON format, located in the `Examples-Cmd/config` directory.

1.  View the configuration example:

    ```sh
    cat config/recorder_json.example
    ```

2.  Create or modify your own configuration file, e.g., `config/my_recorder.json`, ensuring the JSON format is correct.

3.  Full Parameter Description:

    | Parameter                | Type     | Description                                                                                                                                             |
    | ------------------------ | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
    | appId                    | String   | The App ID of the project, must be consistent with the App ID in the RTC SDK.                                                                           |
    | token                    | String   | The channel Token. Required if the channel has security mode enabled.                                                                                   |
    | channelName              | String   | The channel name, must be consistent with the channel name in the RTC SDK.                                                                              |
    | useStringUid             | Boolean  | Whether to use string user IDs.                                                                                                                         |
    | useCloudProxy            | Boolean  | Whether to use the cloud proxy service.                                                                                                                 |
    | userId                   | String   | The user ID for the recorder client.                                                                                                                    |
    | subAllAudio              | Boolean  | Whether to subscribe to all audio streams. If false, specify user IDs in `subAudioUserList`.                                                            |
    | subAudioUserList         | String[] | List of user IDs to subscribe to audio from, effective only when `subAllAudio` is false.                                                                |
    | subAllVideo              | Boolean  | Whether to subscribe to all video streams. If false, specify user IDs in `subVideoUserList`.                                                            |
    | subVideoUserList         | String[] | List of user IDs to subscribe to video from, effective only when `subAllVideo` is false.                                                                |
    | subStreamType            | String   | The type of stream to subscribe to, supports `high` (high-resolution) and `low` (low-resolution).                                                       |
    | isMix                    | Boolean  | Whether to perform mixed-stream recording.                                                                                                              |
    | backgroundColor          | Long     | Background color for mixed-stream recording. Use RGB format (0xRRGGBB), converted to a long value. E.g., Red=0xFF0000, Green=0x00FF00, Blue=0x0000FF.   |
    | backgroundImage          | String   | Path to the background image for mixed-stream recording, supports PNG and JPG. Takes precedence over `backgroundColor` if both are set.                 |
    | layoutMode               | String   | Layout mode for mixed-stream recording, supports `default`, `bestfit`, `vertical`.                                                                      |
    | maxResolutionUid         | String   | In `vertical` layout, sets the user ID whose video is displayed at the maximum resolution.                                                              |
    | recorderStreamType       | String   | Recording type, supports `audio_only`, `video_only`, `both`.                                                                                            |
    | recorderPath             | String   | Recording file path. For mixed-stream, it's the filename; for single-stream, it's the directory where MP4 files named after each user ID will be saved. |
    | maxDuration              | Integer  | Recording duration in seconds.                                                                                                                          |
    | recoverFile              | Boolean  | Whether to write separate h264 and aac files during recording, allowing MP4 recovery if the program crashes.                                            |
    | audio                    | Object   | Audio settings.                                                                                                                                         |
    | audio.sampleRate         | Integer  | Audio sample rate (Hz).                                                                                                                                 |
    | audio.numOfChannels      | Integer  | Number of audio channels.                                                                                                                               |
    | video                    | Object   | Video settings.                                                                                                                                         |
    | video.width              | Integer  | Video width (pixels).                                                                                                                                   |
    | video.height             | Integer  | Video height (pixels).                                                                                                                                  |
    | video.fps                | Integer  | Video frame rate (fps).                                                                                                                                 |
    | waterMark                | Object[] | Watermark settings.                                                                                                                                     |
    | waterMark[].type         | String   | Watermark type, supports `litera` (text), `time` (timestamp), `picture`.                                                                                |
    | waterMark[].litera       | String   | Text content, effective only when type is `litera`.                                                                                                     |
    | waterMark[].fontFilePath | String   | Font file path.                                                                                                                                         |
    | waterMark[].fontSize     | Integer  | Font size.                                                                                                                                              |
    | waterMark[].x            | Integer  | Watermark X coordinate.                                                                                                                                 |
    | waterMark[].y            | Integer  | Watermark Y coordinate.                                                                                                                                 |
    | waterMark[].width        | Integer  | Watermark width.                                                                                                                                        |
    | waterMark[].height       | Integer  | Watermark height.                                                                                                                                       |
    | waterMark[].zorder       | Integer  | Watermark layer order (z-index).                                                                                                                        |
    | waterMark[].imgUrl       | String   | Image watermark URL, effective only when type is `picture`.                                                                                             |
    | encryption               | Object   | Media stream encryption settings.                                                                                                                       |
    | encryption.mode          | String   | Encryption type, supports `AES_128_XTS`, `AES_128_ECB`, `AES_256_XTS`, `SM4_128_ECB`, `AES_128_GCM`, `AES_256_GCM`, `AES_128_GCM2`, `AES_256_GCM2`.     |
    | encryption.key           | String   | Encryption key.                                                                                                                                         |
    | encryption.salt          | String   | Encryption salt, a 32-byte value (often represented as a string).                                                                                       |
    | rotation                 | Object[] | Video rotation settings.                                                                                                                                |
    | rotation[].uid           | String   | User ID whose video needs rotation.                                                                                                                     |
    | rotation[].degree        | Integer  | Rotation angle, supports 0, 90, 180, 270.                                                                                                               |

    > **Important Notes**:
    >
    > - Before executing recording, ensure `appId` and `token` (if applicable) are correctly filled in the JSON.
    > - `appId` and `channelName` must exactly match those used by the RTC SDK clients.
    > - In single-stream recording mode, `recorderPath` specifies a directory path. You must manually ensure this directory exists before starting the recording (e.g., if `"recorderPath": "recorder_result/"`, ensure `Examples-Cmd/recorder_result/` exists).
    > - Ensure the JSON format is correct; do not miss commas, quotes, etc.

#### Start Recording

1.  Create the output directory for single-stream recording (if using):

    ```sh
    mkdir -p Examples-Cmd/recorder_result
    ```

2.  Choose and run the corresponding test script:

    ```sh
    cd Examples-Cmd
    ./script/TestCaseName.sh
    ```

    You can modify the scripts or their corresponding JSON configuration files to customize the recording behavior.

#### Common Test Scripts

The `Examples-Cmd/script` directory provides several preset test scripts:

| Script Name                                      | Description                                                                 |
| ------------------------------------------------ | --------------------------------------------------------------------------- |
| MixStreamRecordingAudioVideo.sh                  | Mixed-stream recording of audio and video.                                  |
| MixStreamRecordingAudio.sh                       | Mixed-stream recording of audio only.                                       |
| MixStreamRecordingVideo.sh                       | Mixed-stream recording of video only.                                       |
| MixStreamRecordingAudioVideoWatermarks.sh        | Mixed-stream recording of audio/video with watermarks.                      |
| MixStreamRecordingAudioVideoWatermarksBg.sh      | Mixed-stream recording of audio/video with watermarks and background.       |
| MixStreamRecordingAudioVideoWatermarksRecover.sh | Mixed-stream recording of audio/video with watermarks and recovery enabled. |
| MixStreamRecordingAudioVideoEncryption.sh        | Mixed-stream recording of audio/video with encryption enabled.              |
| MixStreamRecordingAudioVideoStringUid.sh         | Mixed-stream recording of audio/video using string UIDs.                    |
| SingleStreamRecordingAudioVideo.sh               | Single-stream recording of audio and video.                                 |
| SingleStreamRecordingAudio.sh                    | Single-stream recording of audio only.                                      |
| SingleStreamRecordingVideo.sh                    | Single-stream recording of video only.                                      |
| SingleStreamRecordingAudioVideoWatermarks.sh     | Single-stream recording of audio/video with watermarks.                     |

Choose a suitable script or create custom recording configurations based on these examples. Each script corresponds to a configuration file with the same name in the `config` directory.

#### Stop Recording

- **Start Recording**: Recording starts automatically when the script is executed.
- **Stop Recording**: Enter `1` in the command line and press Enter. The program will stop recording and exit.

#### Recording Output Files

- **Single-Stream Recording**: Generates multiple MP4 files in the `Examples-Cmd/recorder_result/` directory (or as specified by `recorderPath`), named after the UIDs of each user (e.g., `uid_123456_timestamp.mp4`).
- **Mixed-Stream Recording**: Generates a single MP4 file in the `Examples-Cmd` directory (or as specified by `recorderPath`), with the filename specified in the JSON configuration.

#### Troubleshooting Common Issues

- If no recording file is output, check if the AppID, Token, and channel name are correct.
- Ensure there are active users sending media streams in the channel.
- Check the log files for detailed error messages. Logs are typically located in the `Examples-Cmd/logs/` directory.

> **Tip**: For more advanced configuration options and detailed parameter descriptions, refer to the comments in the `Examples-Cmd/config/recorder_json.example` file.

### Recording via API Call

#### Prerequisites

Before starting, ensure you have completed the environment preparation and SDK integration steps, including configuring the JAR and corresponding platform's `.so` files.

#### Implementing Recording via API Call

The following example code, based on the actual example project in the `Examples-Cmd` directory, demonstrates how to use the Recording SDK API for recording.

##### Initialize Service

```java
import io.agora.recording.AgoraMediaRtcRecorder;
import io.agora.recording.AgoraMediaComponentFactory;
import io.agora.recording.AgoraParameter;
import io.agora.recording.AgoraService;
import io.agora.recording.AgoraServiceConfiguration;
import io.agora.recording.Constants;
import io.agora.recording.EncryptionConfig;
import io.agora.recording.IAgoraMediaRtcRecorderEventHandler;
import io.agora.recording.MediaRecorderConfiguration;
import io.agora.recording.RecorderInfo;
import io.agora.recording.RemoteAudioStatistics;
import io.agora.recording.RemoteVideoStatistics;
import io.agora.recording.SpeakVolumeInfo;
import io.agora.recording.VideoMixingLayout;
import io.agora.recording.VideoSubscriptionOptions;
import io.agora.recording.WatermarkConfig;

// Create AgoraService instance
AgoraService agoraService = new AgoraService();

// Configure local proxy. This configuration must be set before initialize.
LocalAccessPointConfiguration localAccessPointConfig = new LocalAccessPointConfiguration();
localAccessPointConfig.setMode(Constants.LocalProxyMode.LocalOnly);
localAccessPointConfig.setDomainList(new String[] { "" });
localAccessPointConfig.setIpList(new String[] { "10.xx.xx.xx" });
localAccessPointConfig.setDomainListSize(1);
localAccessPointConfig.setIpListSize(1);
localAccessPointConfig.setVerifyDomainName("ap.xxx.agora.local");
int setGlobalLocalAccessPointRet = agoraService.setGlobalLocalAccessPoint(localAccessPointConfig);

// Create and configure the service configuration object
AgoraServiceConfiguration config = new AgoraServiceConfiguration();
config.setEnableAudioDevice(false);    // Whether to enable audio device (usually set to false for recording)
config.setEnableAudioProcessor(true);  // Enable audio processing
config.setEnableVideo(true);           // Enable video functionality
config.setAppId("YOUR_APPID");         // Set your App ID
config.setUseStringUid(false);         // Whether to use string UID
agoraService.initialize(config);       // Initialize the service

// Optional: Set cloud proxy
AgoraParameter parameter = agoraService.getAgoraParameter();
if (parameter != null) {
    // Example: Enable cloud proxy (check AgoraParameter documentation for specific keys)
    // parameter.setBool("rtc.enable_proxy", true);
    // parameter.setString("rtc.proxy_server", "your.proxy.server:port");
}
```

##### Join Channel

```java
// Create media component factory
AgoraMediaComponentFactory factory = agoraService.createAgoraMediaComponentFactory();

// Create and initialize the recorder
AgoraMediaRtcRecorder agoraMediaRtcRecorder = factory.createMediaRtcRecorder();
// The second parameter indicates whether to enable mixed-stream recording: true=mixed, false=single
boolean enableMix = false; // Example: Single-stream recording
agoraMediaRtcRecorder.initialize(agoraService, enableMix);

// Create and register the event handler
// Replace AgoraMediaRtcRecorderEventHandler with your actual implementation
IAgoraMediaRtcRecorderEventHandler handler = new AgoraMediaRtcRecorderEventHandler();
agoraMediaRtcRecorder.registerRecorderEventHandler(handler);

// Join the channel
int joinResult = agoraMediaRtcRecorder.joinChannel(
    "YOUR_TOKEN",        // Channel Token, can be null if token validation is not enabled
    "YOUR_CHANNEL_NAME", // Channel name
    "0"                  // User ID, if set to "0", the system will automatically assign one
);
if (joinResult != 0) {
    System.err.println("Failed to join channel, error code: " + joinResult);
    // Handle error appropriately
}
```

##### Configure and Start Recording

```java
// Subscribe to audio streams
boolean subscribeAllAudio = true; // Example: Subscribe to all audio
if (subscribeAllAudio) {
    agoraMediaRtcRecorder.subscribeAllAudio();
} else {
    // Only subscribe to specific users' audio
    agoraMediaRtcRecorder.subscribeAudio("USER_ID_TO_SUBSCRIBE");
}

// Subscribe to video streams
boolean subscribeAllVideo = true; // Example: Subscribe to all video
VideoSubscriptionOptions options = new VideoSubscriptionOptions();
options.setEncodedFrameOnly(false);
options.setType(Constants.VideoStreamType.VIDEO_STREAM_HIGH); // Optional: VIDEO_STREAM_LOW
if (subscribeAllVideo) {
    agoraMediaRtcRecorder.subscribeAllVideo(options);
} else {
    // Only subscribe to specific users' video
    agoraMediaRtcRecorder.subscribeVideo("USER_ID_TO_SUBSCRIBE", options);
}

// Configure mixed-stream layout (only needed in mixed-stream mode)
if (enableMix) {
    VideoMixingLayout layout = new VideoMixingLayout();
    layout.setCanvasWidth(1280);
    layout.setCanvasHeight(720);
    layout.setBackgroundColor(0x000000); // Black background
    // Add user layouts... (See VideoMixingLayout and UserMixerLayout in API reference)
    agoraMediaRtcRecorder.setVideoMixingLayout(layout);
}

// Configure recording parameters
MediaRecorderConfiguration mediaRecorderConfiguration = new MediaRecorderConfiguration();
mediaRecorderConfiguration.setWidth(640);       // Set recording video width
mediaRecorderConfiguration.setHeight(480);      // Set recording video height
mediaRecorderConfiguration.setFps(15);          // Set recording frame rate
mediaRecorderConfiguration.setMaxDurationMs(60 * 60 * 1000); // Max recording duration (e.g., 1 hour) in milliseconds
// IMPORTANT: Ensure the directory exists and is writable
mediaRecorderConfiguration.setStoragePath(enableMix ? "/path/to/save/mixed_recording.mp4" : "/path/to/save/single_stream_dir/"); // Recording file save path/directory

int configResult = 0;
if (enableMix) {
    // Configure mixed-stream recording
    configResult = agoraMediaRtcRecorder.setRecorderConfig(mediaRecorderConfiguration);
} else {
    // Configure single-stream recording (can be called multiple times for different users)
    // Typically called within event handlers like onUserJoined or onFirstRemoteVideoDecoded
    // configResult = agoraMediaRtcRecorder.setRecorderConfigByUid(mediaRecorderConfiguration, "USER_ID");
    // NOTE: For single-stream, setRecorderConfigByUid should be called before startSingleRecordingByUid for each user.
}
if (configResult != 0) {
     System.err.println("Failed to set recorder config, error code: " + configResult);
     // Handle error
}

// Add watermark (Optional)
// WatermarkConfig[] watermarks = new WatermarkConfig[1];
// watermarks[0] = new WatermarkConfig();
// // Configure watermark parameters... (See WatermarkConfig in API reference)
// if (enableMix) {
//     agoraMediaRtcRecorder.enableAndUpdateVideoWatermarks(watermarks);
// } else {
//     agoraMediaRtcRecorder.enableAndUpdateVideoWatermarksByUid(watermarks, "USER_ID");
// }

// Enable encryption (Optional)
boolean enableEncryption = false; // Example: Encryption disabled
if (enableEncryption) {
    EncryptionConfig encryptionConfig = new EncryptionConfig();
    encryptionConfig.setEncryptionMode(Constants.EncryptionMode.AES_128_GCM); // Set encryption mode
    encryptionConfig.setEncryptionKey("YOUR_ENCRYPTION_KEY");
    // encryptionConfig.setEncryptionKdfSalt(...); // Set salt if using GCM2 modes
    agoraMediaRtcRecorder.enableEncryption(true, encryptionConfig);
}

// Start recording
int startResult = 0;
if (enableMix) {
    startResult = agoraMediaRtcRecorder.startRecording();
} else {
    // Start single-stream recording (typically called within event handlers)
    // startResult = agoraMediaRtcRecorder.startSingleRecordingByUid("USER_ID");
}
 if (startResult != 0) {
     System.err.println("Failed to start recording, error code: " + startResult);
     // Handle error
 }
```

##### Handling Recording Events

```java
// Example Implementation of the EventHandler
public static class AgoraMediaRtcRecorderEventHandler implements IAgoraMediaRtcRecorderEventHandler {

    private AgoraMediaRtcRecorder recorder; // Keep a reference if needed
    private boolean isMixMode;

    // Constructor or setter to pass the recorder instance and mode
    public AgoraMediaRtcRecorderEventHandler(AgoraMediaRtcRecorder recorder, boolean isMixMode) {
        this.recorder = recorder;
        this.isMixMode = isMixMode;
    }
     public AgoraMediaRtcRecorderEventHandler() {
        // Default constructor if reference is not needed or set later
    }


    @Override
    public void onConnected(String channelId, String userId) {
         System.out.println("Recorder connected to channel: " + channelId + " with user ID: " + userId);
         // Connection successful, ready for operations
    }

     @Override
    public void onDisconnected(String channelId, String userId, Constants.ConnectionChangedReasonType reason) {
         System.out.println("Recorder disconnected. Reason: " + reason);
    }

    @Override
    public void onUserJoined(String channelId, String userId) {
        System.out.println("Remote user joined: " + userId);
        if (!isMixMode) {
            // In single-stream mode, configure and potentially start recording for the new user
             new Thread(() -> {
                MediaRecorderConfiguration config = new MediaRecorderConfiguration();
                // Configure parameters specific to this user if needed
                config.setWidth(640);
                config.setHeight(480);
                config.setFps(15);
                 // Ensure the directory exists!
                config.setStoragePath("/path/to/save/single_stream_dir/"); // Directory for single stream files
                 // ... other configurations ...

                int configUidResult = recorder.setRecorderConfigByUid(config, userId);
                 if (configUidResult == 0) {
                     // Optionally wait for first frame decoded, or start immediately if configured
                     // int startUidResult = recorder.startSingleRecordingByUid(userId);
                     // System.out.println("Attempted to start single recording for " + userId + ", result: " + startUidResult);
                 } else {
                     System.err.println("Failed to set recorder config for user " + userId + ", error: " + configUidResult);
                 }
             }).start();
        } else {
             // In mixed-stream mode, maybe update the layout
            // updateMixingLayout();
        }
    }

     @Override
     public void onUserLeft(String channelId, String userId, Constants.UserOfflineReasonType reason) {
         System.out.println("Remote user left: " + userId + ", reason: " + reason);
         if (!isMixMode) {
             // Stop single-stream recording for the user who left
             int stopUidResult = recorder.stopSingleRecordingByUid(userId);
             System.out.println("Stopped single recording for user " + userId + ", result: " + stopUidResult);
         } else {
             // In mixed-stream mode, maybe update the layout
             // updateMixingLayout();
         }
     }

    @Override
    public void onFirstRemoteAudioFrame(String channelId, String userId, int elapsed) {
         System.out.println("First remote audio frame received from user: " + userId);
         // If auto-start wasn't used, can start single audio recording here
    }

    @Override
    public void onFirstRemoteVideoDecoded(String channelId, String userId, int width, int height, int elapsed) {
        System.out.println("First remote video decoded from user: " + userId + " [" + width + "x" + height + "]");
        if (!isMixMode) {
            // Good place to start single-stream recording if setRecorderConfigByUid was successful
             new Thread(() -> {
                 int startUidResult = recorder.startSingleRecordingByUid(userId);
                 System.out.println("Started single recording for user " + userId + " after first video frame, result: " + startUidResult);
             }).start();
        }
    }

    @Override
    public void onRecorderStateChanged(String channelId, String userId, Constants.RecorderState state,
            Constants.RecorderReasonCode reason, String fileName) {
        System.out.println("Recorder state changed for user " + (userId != null ? userId : "N/A (Mixed)") +
                           ": State=" + state + ", Reason=" + reason + ", File=" + fileName);
        // Handle state changes, e.g., RECORDER_STATE_ERROR might require action
    }

     @Override
     public void onRecorderInfoUpdated(String channelId, String userId, RecorderInfo info) {
         System.out.println("Recorder info updated for user " + (userId != null ? userId : "N/A (Mixed)") +
                            ": FileName=" + info.getFileName() + ", Duration=" + info.getDurationMs() + "ms, Size=" + info.getFileSize() + " bytes");
     }

     @Override
     public void onEncryptionError(String channelId, Constants.EncryptionErrorType errorType) {
         System.err.println("Encryption error occurred: " + errorType);
     }

    // Implement other necessary event handling methods...
    // e.g., onConnectionLost, onReconnected, onUserVideoStateChanged, etc.
}
```

##### Stop Recording

```java
// Unsubscribe from streams (optional but good practice)
agoraMediaRtcRecorder.unsubscribeAllAudio();
agoraMediaRtcRecorder.unsubscribeAllVideo();

// Stop recording
if (enableMix) {
    agoraMediaRtcRecorder.stopRecording();
} else {
    // Stop single-stream recording for all users being recorded
    // You'll need to keep track of which users are being recorded
    // Example: Assuming you have a list of user IDs called 'recordingUserIds'
    // for (String userId : recordingUserIds) {
    //     agoraMediaRtcRecorder.stopSingleRecordingByUid(userId);
    // }
}

// Unregister the event handler
agoraMediaRtcRecorder.unregisterRecorderEventHandle(handler);

// Leave the channel and release recorder resources
agoraMediaRtcRecorder.leaveChannel();
agoraMediaRtcRecorder.release();

// Release the service
agoraService.release();
```

##### Getting Recorded Files

Recorded files will be saved in different locations based on the recording type:

- **Single-Stream Recording**: Generates MP4 files in the directory specified by `storagePath` in `MediaRecorderConfiguration` (when calling `setRecorderConfigByUid`). Filenames typically start with the UID, e.g., `uid_123456_timestamp.mp4`.
- **Mixed-Stream Recording**: Generates a single MP4 file at the path specified by `storagePath` in `MediaRecorderConfiguration` (when calling `setRecorderConfig`).

In practical applications, it is recommended to set a unique file path for each recording session, possibly using the channel name, timestamp, etc., as part of the filename to avoid overwriting files.

For more recording options and advanced features, please refer to the API documentation for the `MediaRecorderConfiguration` class.

### Run the Maven Example Project

The SDK provides a Spring Boot-based Maven example project for quick verification and secondary development. Below are the basic steps to run the `Examples-Mvn` project:

#### 1. Build the Project

Navigate to the `Examples-Mvn` directory and run:

```sh
mvn clean package
```

After a successful build, `agora-example.jar` will be generated in the `target/` directory.

#### 2. Configure Keys

Create a `.keys` file in the `Examples-Mvn` directory with the following content (replace with your actual values):

```
appId=YOUR_APPID
token=YOUR_TOKEN
```

#### 3. Prepare .so Libraries

Ensure the `libs/native/linux/x86_64/` directory contains all required `.so` files (such as `libagora_rtc_sdk.so`, `librecording.so`, etc.).

#### 4. Run the Example Service

In the `Examples-Mvn` directory, execute:

```sh
LD_LIBRARY_PATH="$LD_LIBRARY_PATH:libs/native/linux/x86_64" java -Dserver.port=18080 -jar target/agora-example.jar
```

- This command starts the Spring Boot service on port 18080.
- To change the port, modify the `-Dserver.port` parameter.

#### 5. Start/Stop Recording via API

- Start recording:
  ```
  http://<server_ip>:18080/api/recording/start?configFileName=mix_stream_recorder_audio_video_water_marks.json
  ```
- Stop recording:
  ```
  http://<server_ip>:18080/api/recording/stop?taskId=<task_id>
  ```

> The recording config file should be placed in the `Examples-Mvn/src/main/resources/` directory.

#### 6. Troubleshooting

- If the service fails to start, check the `.so` file path, `.keys` file content, and port usage.
- If there is no recording output, ensure there are active users in the channel and that AppId/Token/channel name are correct.

## API Reference

For detailed descriptions of the SDK APIs, please refer to the [API-reference.md](API-reference.md) document, each class and method provides detailed parameter descriptions and return value explanations.

## Changelog

### v4.4.150.4 (2025-06-11)

#### API Changes

- **Added**: Added `renewToken` method to `AgoraMediaRtcRecorder` class to support dynamic channel token renewal, preventing recording interruption due to token expiration
- **Changed**: Renamed `Constants.WaterMaskFitMode` to `Constants.WatermarkFitMode` to fix spelling error and maintain naming consistency

#### Improvements & Bug Fixes

- **Fixed**: Fixed `imagePath` property setting issue in `MixerLayoutConfig` class to ensure proper background image path configuration

### v4.4.150.3 (2025-05-20)

#### API Changes

- **Added**: `onError`、`onTokenPrivilegeWillExpire`、`onTokenPrivilegeDidExpire` callback methods in `IAgoraMediaRtcRecorderEventHandler` for error notifications and token expiration notifications.

### v4.4.150.2 (2025-05-09)

#### API Changes

- **Added**: Added the `setGlobalLocalAccessPoint` function to the `AgoraService` class for configuring the global local access point.

#### Improvements & Optimizations

- **Fixed**: Fixed the callback handling issue when packaging with SpringBot.

### v4.4.150.1 (2025-03-28)

#### API Changes

- **Added**: `onEncryptionError` callback in `IAgoraMediaRtcRecorderEventHandler` for encryption error notifications.
- **Added**: `setAudioVolumeIndicationParameters` method in `AgoraMediaRtcRecorder` to configure the interval for remote user volume callbacks.
- **Refactored**: Optimized the parameter structure of the `onAudioVolumeIndication` callback in `IAgoraMediaRtcRecorderEventHandler`.

#### Improvements & Optimizations

- **Enhanced**: Improved `VideoMixingLayout`, fixed `backgroundColor` property.
- **New Feature**: Added `backgroundImage` property to `VideoMixingLayout` to support setting background images.

### v4.4.150-aarch64 (2025-02-24)

#### API Changes

- **Compatibility**: API compatible with v4.4.150.

#### Improvements & Optimizations

- **Platform**: Initial support for ARM64 architecture.

### v4.4.150 (2025-01-21)

#### API Changes

- **Initial Release**: Basic API structure published.

#### Improvements & Optimizations

- **Performance**: Basic functionality and performance optimizations.

## Other References

Refer to the official documentation website for details: (e.g., <https://docs.agora.io/en/recording/java/landing-page>) (Update link as necessary)
