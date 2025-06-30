# Agora Recording Java SDK API Reference

This document provides a detailed description of the API interfaces for the Agora Recording Java SDK, helping developers quickly understand and use the various features of the SDK.

## Table of Contents

- [Core Classes](#core-classes)
  - [AgoraService](#agoraservice)
  - [AgoraMediaComponentFactory](#agoramediacomponentfactory)
  - [AgoraMediaRtcRecorder](#agoramediartcrecorder)
  - [AgoraParameter](#agoraparameter)
- [Observer Interfaces](#observer-interfaces)
  - [IAgoraMediaRtcRecorderEventHandler](#iagoramediartcrecordereventhandler)
- [Data Structures](#data-structures)
  - [AgoraServiceConfiguration](#agoraserviceconfiguration)
  - [MediaRecorderConfiguration](#mediarecorderconfiguration)
  - [EncryptionConfig](#encryptionconfig)
  - [VideoSubscriptionOptions](#videosubscriptionoptions)
  - [VideoMixingLayout](#videomixinglayout)
  - [MixerLayoutConfig](#mixerlayoutconfig)
  - [UserMixerLayout](#usermixerlayout)
  - [WatermarkConfig](#watermarkconfig)
  - [WatermarkOptions](#watermarkoptions)
  - [WatermarkLitera](#watermarklitera)
  - [WatermarkTimestamp](#watermarktimestamp)
  - [WatermarkRatio](#watermarkratio)
  - [LogConfig](#logconfig)
  - [Rectangle](#rectangle)
  - [SpeakVolumeInfo](#speakvolumeinfo)
  - [RemoteVideoStatistics](#remotevideostatistics)
  - [RemoteAudioStatistics](#remoteaudiostatistics)
  - [RecorderInfo](#recorderinfo)
  - [AdvancedConfigInfo](#advancedconfiginfo)
  - [LogUploadServerInfo](#loguploadserverinfo)
  - [LocalAccessPointConfiguration](#localaccesspointconfiguration)
- [Utility Classes](#utility-classes)
  - [Constants](#constants)
  - [Utils](#utils)

## Core Classes

### AgoraService

The `AgoraService` class provides core functionality for initializing and managing Agora services, serving as the main entry point for using Agora recording features.

#### Main Methods

##### `AgoraService()`

Constructs an `AgoraService` instance and initializes native components. Only one `AgoraService` instance can be initialized at a time.

##### `int initialize(AgoraServiceConfiguration config)`

Initializes the `AgoraService` object with the specified configuration.

**Parameters**:

- `config`: An `AgoraServiceConfiguration` object containing initialization parameters

**Return Value**:

- `0`: Success
- `< 0`: Failure, specific error codes might include:
  - `ERR_INVALID_ARGUMENT (-2)`: If `context` in `AgoraServiceConfiguration` is not provided (Android only).
  - `ERR_INIT_NET_ENGINE (-?)`: If the network engine cannot be initialized (e.g., firewall blocking).

##### `AgoraMediaComponentFactory createAgoraMediaComponentFactory()`

Creates and returns an `AgoraMediaComponentFactory` object used for creating media components.

**Return Value**:

- Returns an `AgoraMediaComponentFactory` instance

##### `AgoraParameter getAgoraParameter()`

Creates and returns an `AgoraParameter` object used for parameter management.

**Return Value**:

- Returns an `AgoraParameter` instance on success
- Returns `null` on failure

##### `int setLogFile(String filePath, int fileSize)`

Sets the path and size of the SDK log files. The SDK records logs in two files, each with a default size of 512 KB. If `fileSize` is set to 1024 KB, the maximum size for logs will be 2 MB. If the total size exceeds the limit, new logs overwrite old ones.

**Note:** Call this method immediately after `initialize` to ensure complete logs.

**Parameters**:

- `filePath`: Path of the log file. Ensure the directory exists and is writable.
- `fileSize`: Size of each SDK log file in bytes.

**Return Value**:

- `0`: Success
- `< 0`: Failure

##### `int setLogLevel(Constants.LogLevel level)`

Sets the level of the SDK log output. Logs at or above the selected level will be output. The sequence is OFF, CRITICAL, ERROR, WARNING, INFO, DEBUG.

**Parameters**:

- `level`: Level of the log file. See {@link Constants.LogLevel}.
  - `LOG_LEVEL_NONE (0x0000)`: No log output.
  - `LOG_LEVEL_INFO (0x0001)`: (Recommended) Output INFO level logs and above.
  - `LOG_LEVEL_WARN (0x0002)`: Output WARN level logs and above.
  - `LOG_LEVEL_ERROR (0x0004)`: Output ERROR level logs and above.
  - `LOG_LEVEL_FATAL (0x0008)`: Output FATAL level logs.

**Return Value**:

- `0`: Success
- `< 0`: Failure

##### `int release()`

Releases the `AgoraService` object and its associated resources. The instance becomes invalid after calling this method.

**Return Value**:

- `0`: Success
- `< 0`: Failure

##### `int setGlobalLocalAccessPoint(LocalAccessPointConfiguration config)`

Sets global local access point addresses in local AP mode (which also calls local proxy).

**Note:** This method must be called before `initialize(AgoraServiceConfiguration)` of AgoraService. It will affect all recorder instances in the same process, and only needs to be called once per process.

**Parameters**:

- `config`: The {@link LocalAccessPointConfiguration} object. See the definition of LocalAccessPointConfiguration for details.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

### AgoraMediaComponentFactory

The `AgoraMediaComponentFactory` class is a factory class for creating Agora media components. This class provides functionality to create media recording component instances.

#### Main Methods

##### `AgoraMediaRtcRecorder createMediaRtcRecorder()`

Creates a new `AgoraMediaRtcRecorder` instance.

**Return Value**:

- A new `AgoraMediaRtcRecorder` instance

**Exceptions**:

- `RuntimeException`: If native recorder creation fails.

##### `int release()`

Releases local resources associated with the factory.

**Return Value**:

- `0`: Success
- `< 0`: Failure

### AgoraMediaRtcRecorder

The `AgoraMediaRtcRecorder` class provides functionality for recording Agora RTC media streams. This class allows recording audio and video streams from Agora RTC channels and provides options for stream mixing, encryption, and selective subscription.

#### Main Methods

##### `int initialize(AgoraService service, boolean enableMix)`

Initializes the recorder with the specified service and mixing settings.

**Parameters**:

- `service`: An Agora service instance that must be initialized before calling this method.
- `enableMix`: Whether to enable stream mixing.

**Return Value**:

- `0`: Initialization successful.
- `< 0`: Initialization failed.

##### `int joinChannel(String token, String channelName, String userId)`

Joins an Agora RTC channel.

**Parameters**:

- `token`: Token for authentication.
- `channelName`: Name of the channel to join. Must not exceed 64 bytes and can contain lowercase/uppercase letters, numbers, spaces, and special characters `!#$%&()+,-.:;<=>?@[]^_{|}~`.
- `userId`: User ID of the local user. If set to `null` or `"0"`, the system will automatically assign one.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int leaveChannel()`

Leaves the current channel.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int enableEncryption(boolean enabled, EncryptionConfig config)`

Enables or disables built-in encryption. Recommended to call before joining a channel for high security. All users in the same channel must use the same mode and key. The key is cleared when all users leave.

**Note:** Enabling encryption disables RTMP streaming.

**Parameters**:

- `enabled`: Whether to enable built-in encryption.
- `config`: Encryption configuration parameters. See {@link EncryptionConfig}.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int subscribeAllAudio()`

Subscribes to audio streams of all remote users in the channel. Automatically subscribes to new users joining later.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int subscribeAllVideo(VideoSubscriptionOptions options)`

Subscribes to video streams of all remote users in the channel. Automatically subscribes to new users joining later.

**Parameters**:

- `options`: Video subscription options, including stream type and other parameters. See {@link VideoSubscriptionOptions}.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int unsubscribeAllAudio()`

Stops subscribing to the audio streams of all remote users. Automatically stops subscribing to new users unless `subscribeAudio` or `subscribeAllAudio` is called again.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int unsubscribeAllVideo()`

Stops subscribing to the video streams of all remote users. Automatically stops subscribing to new users unless `subscribeVideo` or `subscribeAllVideo` is called again.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int subscribeAudio(String userId)`

Subscribes to the audio stream of a specified remote user.

**Parameters**:

- `userId`: The ID of the remote user whose audio to subscribe to.

**Return Value**:

- `0`: Method call successful.
- `-2`: Invalid userId.
- Other `< 0`: Method call failed.

##### `int unsubscribeAudio(String userId)`

Stops subscribing to the audio stream of a specified remote user.

**Parameters**:

- `userId`: The ID of the remote user whose audio to unsubscribe from.

**Return Value**:

- `0`: Method call successful.
- `-2`: Invalid userId.
- Other `< 0`: Method call failed.

##### `int subscribeVideo(String userId, VideoSubscriptionOptions options)`

Subscribes to the video stream of a specified remote user.

**Parameters**:

- `userId`: The ID of the remote user whose video to subscribe to.
- `options`: Video subscription options. See {@link VideoSubscriptionOptions}.

**Return Value**:

- `0`: Method call successful.
- `-2`: Invalid userId.
- Other `< 0`: Method call failed.

##### `int unsubscribeVideo(String userId)`

Stops subscribing to the video stream of a specified remote user.

**Parameters**:

- `userId`: The ID of the remote user whose video to unsubscribe from.

**Return Value**:

- `0`: Method call successful.
- `-2`: Invalid userId.
- Other `< 0`: Method call failed.

##### `int setVideoMixingLayout(VideoMixingLayout layout)`

Sets the layout for mixed video streams.

**Parameters**:

- `layout`: Layout configuration for mixed video streams. See {@link VideoMixingLayout}.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int setRecorderConfig(MediaRecorderConfiguration config)`

Configures recorder settings. This method must be called before starting recording.

**Parameters**:

- `config`: Recorder configuration parameters. See {@link MediaRecorderConfiguration}.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int setRecorderConfigByUid(MediaRecorderConfiguration config, String userId)`

Configures the recorder settings for a specified user (for single-stream recording).

**Parameters**:

- `config`: Recorder configuration parameters. See {@link MediaRecorderConfiguration}.
- `userId`: The user ID for the user whose recording settings to configure.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int startRecording()`

Starts the recording process (mixed-stream or previously configured single-streams). Make sure to configure the recorder using `setRecorderConfig` (for mixed) or `setRecorderConfigByUid` (for single) before calling this method.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int stopRecording()`

Stops the mixed-stream recording process and saves the recorded file.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int startSingleRecordingByUid(String userId)`

Starts the single-stream recording process for a specified user. Ensure `setRecorderConfigByUid` was called for this user.

**Parameters**:

- `userId`: User ID to start recording for.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int stopSingleRecordingByUid(String userId)`

Stops the single-stream recording process for a specified user and saves the file.

**Parameters**:

- `userId`: User ID to stop recording for.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int registerRecorderEventHandler(IAgoraMediaRtcRecorderEventHandler handler)`

Registers an event handler for recording events.

**Parameters**:

- `handler`: Event handler implementing the `IAgoraMediaRtcRecorderEventHandler` interface.

**Return Value**:

- `0`: Registration successful.
- `< 0`: Registration failed.

##### `int unregisterRecorderEventHandle(IAgoraMediaRtcRecorderEventHandler handler)`

Unregisters the event handler.

**Parameters**:

- `handler`: The event handler previously registered.

**Return Value**:

- `0`: Unregistration successful.
- `< 0`: Unregistration failed.

##### `int enableAndUpdateVideoWatermarks(WatermarkConfig[] watermarkConfigs)`

Adds or updates watermarks for the mixed-stream video.

**Parameters**:

- `watermarkConfigs`: Watermark configurations. See {@link WatermarkConfig}.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int disableVideoWatermarks()`

Disables watermarks for the mixed-stream video.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int enableAndUpdateVideoWatermarksByUid(WatermarkConfig[] watermarkConfigs, String userId)`

Adds or updates watermarks for a specific user's single-stream video.

**Parameters**:

- `watermarkConfigs`: Watermark configurations. See {@link WatermarkConfig}.
- `userId`: The user ID whose video stream the watermarks should be applied to.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int disableVideoWatermarksByUid(String userId)`

Disables watermarks for a specific user's single-stream video.

**Parameters**:

- `userId`: The user ID whose video stream watermarks should be disabled.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int setAudioVolumeIndicationParameters(int intervalInMs)`

Sets the interval for the `onAudioVolumeIndication` callback.

**Parameters**:

- `intervalInMs`: Interval in milliseconds. Default is 500.
  - `<= 10`: Disables the callback.
  - `> 10`: Interval between callbacks.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

##### `int renewToken(String token)`

Renews the token for the current session.
The token expires after a certain period of time. When the `IAgoraMediaRtcRecorderEventHandler#onError(String, Constants.ErrorCodeType, String)` callback reports `Constants.ErrorCodeType#ERR_TOKEN_EXPIRED (109)`, or when `IAgoraMediaRtcRecorderEventHandler#onTokenPrivilegeWillExpire(String, String)` or `IAgoraMediaRtcRecorderEventHandler#onTokenPrivilegeDidExpire(String)` is triggered, you must generate a new token on your server and call this method to renew it. Otherwise, the SDK will disconnect from the Agora channel.

**Parameters**:

- `token`: The new token generated from your server.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed (e.g., if the token is null or empty).

##### `int release()`

Releases the recorder instance and associated resources.

**Return Value**:

- `0`: Method call successful.
- `< 0`: Method call failed.

### AgoraParameter

The `AgoraParameter` class provides an advanced parameter setting interface that allows you to access and modify additional features of the SDK at runtime. It supports boolean, integer, unsigned integer, double, string, object (JSON), and array (JSON) types.

#### Main Methods

##### `int setBool(String key, boolean value)`

Sets a boolean parameter.

**Parameters**:

- `key`: Parameter name.
- `value`: Parameter value.

**Return Value**:

- `0`: Setting successful.
- `< 0`: Setting failed.

##### `int setInt(String key, int value)`

Sets an integer parameter.

**Parameters**:

- `key`: Parameter name.
- `value`: Parameter value.

**Return Value**:

- `0`: Setting successful.
- `< 0`: Setting failed.

##### `int setUInt(String key, int value)`

Sets an unsigned integer parameter.

**Parameters**:

- `key`: Parameter name.
- `value`: Parameter value (use Java `int`).

**Return Value**:

- `0`: Setting successful.
- `< 0`: Setting failed.

##### `int setNumber(String key, double value)`

Sets a floating-point number parameter.

**Parameters**:

- `key`: Parameter name.
- `value`: Parameter value.

**Return Value**:

- `0`: Setting successful.
- `< 0`: Setting failed.

##### `int setString(String key, String value)`

Sets a string parameter.

**Parameters**:

- `key`: Parameter name.
- `value`: Parameter value.

**Return Value**:

- `0`: Setting successful.
- `< 0`: Setting failed.

##### `int setObject(String key, String value)`

Sets an object parameter value using a JSON string.

**Parameters**:

- `key`: Parameter name.
- `value`: JSON string representing the object.

**Return Value**:

- `0`: Setting successful.
- `< 0`: Setting failed.

##### `int setArray(String key, String value)`

Sets an array parameter value using a JSON string.

**Parameters**:

- `key`: Parameter name.
- `value`: JSON string representing the array.

**Return Value**:

- `0`: Setting successful.
- `< 0`: Setting failed.

##### `boolean getBool(String key)`

Retrieves a boolean parameter value.

**Parameters**:

- `key`: Parameter name.

**Return Value**:

- The boolean value associated with the key.

**Exceptions**:

- `IllegalStateException`: If the parameter cannot be retrieved.

##### `int getInt(String key)`

Retrieves an integer parameter value.

**Parameters**:

- `key`: Parameter name.

**Return Value**:

- The integer value associated with the key.

**Exceptions**:

- `IllegalStateException`: If the parameter cannot be retrieved.

##### `int getUInt(String key)`

Retrieves an unsigned integer parameter value.

**Parameters**:

- `key`: Parameter name.

**Return Value**:

- The unsigned integer value associated with the key (returned as Java `int`).

**Exceptions**:

- `IllegalStateException`: If the parameter cannot be retrieved.

##### `double getNumber(String key)`

Retrieves a floating-point number parameter value.

**Parameters**:

- `key`: Parameter name.

**Return Value**:

- The double value associated with the key.

**Exceptions**:

- `IllegalStateException`: If the parameter cannot be retrieved.

##### `String getString(String key)`

Retrieves a string parameter value.

**Parameters**:

- `key`: Parameter name.

**Return Value**:

- The string value associated with the key, or `null` if not found.

##### `String getObject(String key)`

Retrieves an object parameter value as a JSON string.

**Parameters**:

- `key`: Parameter name.

**Return Value**:

- The JSON string representing the object, or `null` if not found.

##### `String getArray(String key, String args)`

Retrieves an array parameter value as a JSON string.

**Parameters**:

- `key`: Parameter name.
- `args`: Additional arguments for array retrieval (specific usage may vary).

**Return Value**:

- The JSON string representing the array, or `null` if not found.

##### `int setParameters(String parameters)`

Sets multiple parameters at once using a JSON string.

**Parameters**:

- `parameters`: A JSON string containing multiple parameter key-value pairs.

**Return Value**:

- `0`: Setting successful.
- `< 0`: Setting failed.

##### `String convertPath(String filePath)`

Converts a file path to a platform-specific format.

**Parameters**:

- `filePath`: The original file path to convert.

**Return Value**:

- The converted platform-specific file path, or `null` if conversion fails.

##### `void release()`

Releases all resources associated with this parameter instance. The instance becomes invalid after this call.

## Observer Interfaces

### IAgoraMediaRtcRecorderEventHandler

The `IAgoraMediaRtcRecorderEventHandler` interface defines callback methods for connection state changes between the SDK and Agora channels, as well as other recording-related event callbacks.

#### Main Callback Methods

##### `void onConnected(String channelId, String userId)`

Triggered when the connection state of the SDK to the Agora channel changes to `CONNECTION_STATE_CONNECTED(3)`.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: User ID.

##### `void onDisconnected(String channelId, String userId, Constants.ConnectionChangedReasonType reason)`

Triggered when the connection state of the SDK to the Agora channel changes to `CONNECTION_STATE_DISCONNECTED(1)`.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: User ID.
- `reason`: Reason for the connection state change. See {@link Constants.ConnectionChangedReasonType}.

##### `void onReconnected(String channelId, String userId, Constants.ConnectionChangedReasonType reason)`

Triggered when the connection state changes to `CONNECTION_STATE_CONNECTED(3)` again after being disconnected.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: User ID.
- `reason`: Reason for the connection state change. See {@link Constants.ConnectionChangedReasonType}.

##### `void onConnectionLost(String channelId, String userId)`

Triggered when the SDK loses connection with the Agora channel.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: User ID.

##### `void onUserJoined(String channelId, String userId)`

Triggered when a remote user joins the channel.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: ID of the remote user who joined the channel.

##### `void onUserLeft(String channelId, String userId, Constants.UserOfflineReasonType reason)`

Triggered when a remote user leaves the channel.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: ID of the remote user who left the channel.
- `reason`: Reason why the user left. See {@link Constants.UserOfflineReasonType}.

##### `void onUserVideoStateChanged(String channelId, String userId, Constants.RemoteVideoState state, Constants.RemoteVideoStateReason reason, int elapsed)`

Triggered when the state of a remote user's video stream changes.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: ID of the remote user whose video state has changed.
- `state`: Current state of the video. See {@link Constants.RemoteVideoState}.
- `reason`: Reason for the state change. See {@link Constants.RemoteVideoStateReason}.
- `elapsed`: Time elapsed (ms) from the local user calling `joinChannel` until this event occurs.

##### `void onUserAudioStateChanged(String channelId, String userId, Constants.RemoteAudioState state, Constants.RemoteAudioStateReason reason, int elapsed)`

Triggered when the state of a remote user's audio stream changes.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: ID of the remote user whose audio state has changed.
- `state`: Current state of the audio. See {@link Constants.RemoteAudioState}.
- `reason`: Reason for the state change. See {@link Constants.RemoteAudioStateReason}.
- `elapsed`: Time elapsed (ms) from the local user calling `joinChannel` until this event occurs.

##### `void onFirstRemoteVideoDecoded(String channelId, String userId, int width, int height, int elapsed)`

Triggered when the SDK decodes the first frame of a remote video stream.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: Remote user ID.
- `width`: Video width (pixels).
- `height`: Video height (pixels).
- `elapsed`: Time elapsed (ms) from the local user calling `joinChannel` until this event occurs.

##### `void onFirstRemoteAudioDecoded(String channelId, String userId, int elapsed)`

Triggered when the SDK decodes the first frame of a remote audio stream for playback.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: Remote user ID sending the audio stream.
- `elapsed`: Time elapsed (ms) from the local user calling `joinChannel` until this event occurs.

##### `void onAudioVolumeIndication(String channelId, SpeakVolumeInfo[] speakers, int speakerNumber)`

Reports which users are speaking and their volumes.

**Parameters**:

- `channelId`: Channel ID.
- `speakers`: Array of volume information for each speaker. See {@link SpeakVolumeInfo}.
- `speakerNumber`: Total number of speakers.

##### `void onActiveSpeaker(String channelId, String userId)`

Triggered when an active speaker is detected (user speaking at the highest volume).

**Parameters**:

- `channelId`: Channel ID.
- `userId`: ID of the active speaker. A `userId` of `"0"` means the local user.

##### `void onRemoteVideoStats(String channelId, String userId, RemoteVideoStatistics stats)`

Reports the statistics of a remote video stream.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: ID of the remote user.
- `stats`: Current statistics of the video. See {@link RemoteVideoStatistics}.

##### `void onRemoteAudioStats(String channelId, String userId, RemoteAudioStatistics stats)`

Reports the statistics of a remote audio stream.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: ID of the remote user.
- `stats`: Current statistics of the audio. See {@link RemoteAudioStatistics}.

##### `void onRecorderStateChanged(String channelId, String userId, Constants.RecorderState state, Constants.RecorderReasonCode reason, String fileName)`

Triggered when the recording state changes.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: User ID.
- `state`: Current recording state. See {@link Constants.RecorderState}.
- `reason`: Reason for the recording state change. See {@link Constants.RecorderReasonCode}.
- `fileName`: Name of the recording file.

##### `void onRecorderInfoUpdated(String channelId, String userId, RecorderInfo info)`

Triggered periodically to report updated recording information (filename, duration, size) based on `recorderInfoUpdateInterval` in `MediaRecorderConfiguration`.

**Parameters**:

- `channelId`: Channel ID.
- `userId`: User ID.
- `info`: Information about the recording file. See {@link RecorderInfo}.

##### `void onEncryptionError(String channelId, Constants.EncryptionErrorType errorType)`

Triggered when an error occurs in the encryption process.

**Parameters**:

- `channelId`: Channel ID.
- `errorType`: Type of encryption error. See {@link Constants.EncryptionErrorType}.

##### `void onError(String channelId, Constants.ErrorCodeType error, String message)`

Reports the error code and error message.

**Parameters**:

- `channelId`: The channel ID.
- `error`: The error code. See {@link io.agora.recording.Constants.ErrorCodeType ErrorCode} for details.
- `message`: Error message.

##### `void onTokenPrivilegeWillExpire(String channelId, String token)`

Occurs when the token privilege is about to expire in 30 seconds.

<p>
The SDK triggers this callback to remind the app to get a new token before
the token privilege expires.
<p>
Upon receiving this callback, you must generate a new token on your server
and call
{@link AgoraMediaRtcRecorder#renewToken(String)} to renew it.

**Parameters**:

- `channelId`: The channel ID.
- `token`: The token that is about to expire in 30 seconds.

##### `void onTokenPrivilegeDidExpire(String channelId)`

Occurs when the token has expired.

<p>
Upon receiving this callback, you must generate a new token on your server
and call
{@link AgoraMediaRtcRecorder#renewToken(String)} to renew it.

**Parameters**:

- `channelId`: The channel ID.

## Data Structures

### AgoraServiceConfiguration

The `AgoraServiceConfiguration` class is used to configure and initialize Agora service instances. This class contains all settings required to initialize and configure an Agora service instance.

#### Main Properties

- **enableAudioProcessor**: Whether to enable the audio processing module. Default: `true`. Disabling prevents audio track creation.
- **enableAudioDevice**: Whether to enable the audio device module (for managing audio recording/playback devices). Default: `true`. Disabling prevents audio recording/playback via devices, but PCM data can still be pushed if `enableAudioProcessor` is true.
- **enableVideo**: Whether to enable video. Default: `false`.
- **context**: The user context. For Android, it is the `Context` of an Activity. (Android only).
- **appId**: The App ID of the project.
- **areaCode**: Supported area code. Default: `AREA_CODE_GLOB`. See {@link Constants.AreaCode}.
- **channelProfile**: Channel profile. Default: `CHANNEL_PROFILE_LIVE_BROADCASTING`. See {@link Constants.ChannelProfileType}.
- **license**: License for verification when connecting to a channel.
- **audioScenario**: Audio scenario. Default: `AUDIO_SCENARIO_DEFAULT`. See {@link Constants.AudioScenarioType}.
- **logConfig**: User-defined log configuration. See {@link LogConfig}.
- **useStringUid**: Whether to enable string user IDs. Default: `false`.
- **useExternalEglContext**: Whether to use the EGL context in the current thread as the SDK's root EGL context. (Android only). Default: `false`.
- **domainLimit**: Whether to enable domain restrictions (only connect to servers parsed by DNS). Default: `false`.

### MediaRecorderConfiguration

The `MediaRecorderConfiguration` class is used to configure video and audio parameters for recording. It includes settings such as storage path, container format, stream type, maximum duration, and update interval.

#### Main Properties

- **storagePath**: The absolute path (including filename and extension) of the recording file. Ensure the directory exists and is writable. Example paths:
  - Windows: `C:/Users/userName/AppData/Local/Agora/process_name/example.mp4`
  - Linux: `result/example.mp4`
  - Android: `/storage/emulated/0/Android/data/package_name/files/example.mp4`
  - macOS: `/Library/Logs/example.mp4`
  - iOS: `/App Sandbox/Library/Caches/example.mp4`
- **containerFormat**: The format of the recording file. Default: `FORMAT_MP4`. See {@link Constants.MediaRecorderContainerFormat}.
- **streamType**: The recording content. Default: `STREAM_TYPE_BOTH`. See {@link Constants.MediaRecorderStreamType}.
- **maxDurationMs**: Maximum recording duration in milliseconds. Default: `120000`.
- **recorderInfoUpdateInterval**: Interval (ms) for the `onRecorderInfoUpdated` callback. Range: [1000, 10000]. Default: `0` (disabled).
- **width**: Width of the recorded video in pixels. Default: `1280`.
- **height**: Height of the recorded video in pixels. Default: `720`.
- **fps**: Frame rate of the recorded video (fps). Default: `30`.
- **sampleRate**: Sample rate of the recorded audio (Hz). Default: `48000`.
- **channelNum**: Number of audio channels. Default: `1` (mono).
- **videoSourceType**: Video source type for recording. Default: `VIDEO_SOURCE_CAMERA_PRIMARY`. See {@link Constants.VideoSourceType}.

### EncryptionConfig

The `EncryptionConfig` class is used to configure media stream encryption options.

#### Main Properties

- **encryptionMode**: Encryption mode. Default: `AES_128_GCM2`. See {@link Constants.EncryptionMode}.
- **encryptionKey**: Encryption key in string format. Setting to `null` or not setting disables encryption (returns `ERR_INVALID_ARGUMENT`).
- **encryptionKdfSalt**: Encryption salt value (byte array). Default: 32 zeros. Must be 32 bytes long when setting.

### VideoSubscriptionOptions

The `VideoSubscriptionOptions` class is used to configure video subscription options.

#### Main Properties

- **type**: Type of video stream to subscribe to. Default: `VIDEO_STREAM_HIGH`. See {@link Constants.VideoStreamType}.
- **encodedFrameOnly**: Whether to receive only encoded frames. Default: `false`.

### VideoMixingLayout

The `VideoMixingLayout` class is used to configure the layout of mixed videos. It includes canvas dimensions, frame rate, background color/image, and user layout configurations.

#### Main Properties

- **canvasWidth**: Canvas width in pixels.
- **canvasHeight**: Canvas height in pixels.
- **canvasFps**: Frame rate of the canvas (output mixed video). Default: 0.
- **backgroundColor**: Background color of the canvas (long value, e.g., 0xFF0000 for red). Default: 0.
- **backgroundImage**: Background image URL for the canvas. Takes precedence over `backgroundColor`. Default: `null`.
- **userLayoutConfigs**: Array of layout configurations for each user in the mix. See {@link UserMixerLayout}. Default: `null`.

### MixerLayoutConfig

The `MixerLayoutConfig` class provides the configuration for the layout of a mixer.

#### Main Properties

- **x**: X-coordinate of the top-left corner. Default: `0`.
- **y**: Y-coordinate of the top-left corner. Default: `0`.
- **width**: Layout width. Default: `0`.
- **height**: Layout height. Default: `0`.
- **zOrder**: Display layer. Higher values are drawn on top. Default: `0`.
- **alpha**: Transparency level (0.0: completely transparent, 1.0: completely opaque). Default: `1.0f`.
- **mirror**: Whether the layout is mirrored horizontally. Default: `false`.
- **imagePath**: File path of the background image for the layout. Default: `null`.
- **rotation**: Rotation angle (0: none, 1: 90, 2: 180, 3: 270 degrees). Default: `0`.

### UserMixerLayout

The `UserMixerLayout` class represents the layout configuration for a specific user in a mixed stream. It associates a user ID with their layout settings.

#### Main Properties

- **userId**: The ID of the user.
- **config**: The layout configuration settings for this user. See {@link MixerLayoutConfig}.

### WatermarkConfig

The `WatermarkConfig` class is used to configure watermark options.

#### Main Properties

- **index**: Watermark index. Default: `0`.
- **type**: Watermark source type. Default: `WatermarkSourceType.PICTURE`. See {@link Constants.WatermarkSourceType}.
- **timestampSource**: Timestamp watermark configuration. See {@link WatermarkTimestamp}. Default: `null`.
- **literaSource**: Text (literal) watermark configuration. See {@link WatermarkLitera}. Default: `null`.
- **imageUrl**: Image watermark URL. Default: `null`.
- **options**: Watermark display options. See {@link WatermarkOptions}. Default: `null`.

### WatermarkOptions

The `WatermarkOptions` class is used to configure watermark display options.

#### Main Properties

- **visibleInPreview**: Whether the watermark image is visible in the local video preview. Default: `true`.
- **mode**: Watermark adaptation mode. Default: `FIT_MODE_COVER_POSITION`. See {@link Constants.WatermarkFitMode}.
- **positionInLandscapeMode**: Used to set the watermark area in landscape mode when `mode` is `FIT_MODE_COVER_POSITION`. See {@link Rectangle}. Default: (0, 0, 0, 0).
- **positionInPortraitMode**: Used to set the watermark area in portrait mode when `mode` is `FIT_MODE_COVER_POSITION`. See {@link Rectangle}. Default: (0, 0, 0, 0).
- **watermarkRatio**: Used to set watermark coordinates when `mode` is `FIT_MODE_USE_IMAGE_RATIO`. See {@link WatermarkRatio}. Default: Default `WatermarkRatio` object.
- **zOrder**: Watermark display layer order. Default: `0`.

### WatermarkLitera

The `WatermarkLitera` class is used to configure text (literal) watermark options.

#### Main Properties

- **wmLitera**: Watermark text content. Default: `null`.
- **fontFilePath**: Font file path. Default: `null` (uses system font).
- **fontSize**: Font size. Default: `10`.

### WatermarkTimestamp

The `WatermarkTimestamp` class is used to configure timestamp watermark options.

#### Main Properties

- **fontSize**: Font size. Default: `10`.
- **fontFilePath**: Font file path. Default: `null` (uses system font).

### WatermarkRatio

The `WatermarkRatio` class defines the position and size of a watermark using ratios relative to the video canvas.

#### Main Properties

- **xRatio**: Horizontal position ratio (0.0-1.0) of the top-left corner. Default: `0.0f`.
- **yRatio**: Vertical position ratio (0.0-1.0) of the top-left corner. Default: `0.0f`.
- **widthRatio**: Width ratio (0.0-1.0) relative to the canvas width. Default: `0.0f`.

### LogConfig

The `LogConfig` class provides the configuration for logging.

#### Main Properties

- **filePath**: The log file path. Default: `null` (uses default SDK log path).
- **fileSizeInKB**: The size of each log file in KB. Default: `1024`.
- **level**: The log level. Default: `LOG_LEVEL_INFO`. See {@link Constants.LogLevel}.

### Rectangle

The `Rectangle` class is used to configure rectangle options.

#### Main Properties

- **x**: X-coordinate of the top-left corner.
- **y**: Y-coordinate of the top-left corner.
- **width**: Width of the rectangle.
- **height**: Height of the rectangle.

### SpeakVolumeInfo

The `SpeakVolumeInfo` class contains volume information for speakers.

#### Main Properties

- **userId**: User ID of the speaker.
- **volume**: Volume of the speaker (0-255).

### RemoteVideoStatistics

The `RemoteVideoStatistics` class provides statistics for remote video streams.

#### Main Properties

- **delay**: The delay in milliseconds.
- **width**: The width of the video frame in pixels.
- **height**: The height of the video frame in pixels.
- **receivedBitrate**: The received bitrate in Kbps.
- **decoderOutputFrameRate**: The frame rate of the decoded video output.
- **rxStreamType**: The type of the received video stream. See {@link Constants.VideoStreamType}.

### RemoteAudioStatistics

The `RemoteAudioStatistics` class contains statistics for a remote audio stream.

#### Main Properties

- **quality**: The quality of the audio stream (0: unknown, 1: excellent, 2: good, 3: poor, 4: bad, 5: very bad).
- **networkTransportDelay**: The network transport delay in milliseconds.
- **jitterBufferDelay**: The jitter buffer delay in milliseconds.
- **audioLossRate**: The audio loss rate in percentage.

### RecorderInfo

The `RecorderInfo` class contains recording information.

#### Main Properties

- **fileName**: The absolute path of the recording file.
- **durationMs**: The recording duration in milliseconds.
- **fileSize**: The size in bytes of the recording file.

### AdvancedConfigInfo

The `AdvancedConfigInfo` class is used for advanced configuration, currently mainly for log upload server settings.

#### Main Properties

- **logUploadServer**: The log upload server configuration. See {@link LogUploadServerInfo}.

#### Main Methods

- `getLogUploadServer()`: Gets the log upload server configuration.
- `setLogUploadServer(LogUploadServerInfo logUploadServer)`: Sets the log upload server configuration.

### LogUploadServerInfo

The `LogUploadServerInfo` class describes the log upload server information.

#### Main Properties

- **serverDomain**: The log upload server domain.
- **serverPath**: The log upload server path.
- **serverPort**: The log upload server port.
- **serverHttps**: Whether to use HTTPS requests (`true` for HTTPS, `false` for HTTP).

#### Main Methods

- `getServerDomain()/setServerDomain(String)`: Get/set the server domain.
- `getServerPath()/setServerPath(String)`: Get/set the server path.
- `getServerPort()/setServerPort(int)`: Get/set the server port.
- `isServerHttps()/setServerHttps(boolean)`: Get/set whether to use HTTPS.

### LocalAccessPointConfiguration

The `LocalAccessPointConfiguration` class is used to configure local proxy access points.

#### Main Properties

- **ipList**: Local access point IP address list.
- **ipListSize**: Number of IP addresses.
- **domainList**: Local access point domain list.
- **domainListSize**: Number of domains.
- **verifyDomainName**: Certificate domain name installed on the specific local access point. An empty string means using the SNI domain.
- **mode**: Proxy connection mode. See {@link Constants.LocalProxyMode}.
- **advancedConfig**: Advanced configuration. See {@link AdvancedConfigInfo}.
- **disableAut**: Whether to disable vos-aut (default: `true`).

#### Main Methods

- `getIpList()/setIpList(String[])`: Get/set the IP address list.
- `getIpListSize()/setIpListSize(int)`: Get/set the number of IP addresses.
- `getDomainList()/setDomainList(String[])`: Get/set the domain list.
- `getDomainListSize()/setDomainListSize(int)`: Get/set the number of domains.
- `getVerifyDomainName()/setVerifyDomainName(String)`: Get/set the certificate domain name.
- `getMode()/setMode(Constants.LocalProxyMode)`: Get/set the proxy connection mode.
- `getAdvancedConfig()/setAdvancedConfig(AdvancedConfigInfo)`: Get/set the advanced configuration.
- `isDisableAut()/setDisableAut(boolean)`: Get/set whether to disable vos-aut.

#### Usage Note

- `LocalAccessPointConfiguration` can be used in the `AgoraService#setGlobalLocalAccessPoint` method, which affects all recorder instances in the same process.
- `AdvancedConfigInfo` is currently mainly used for log upload server configuration and can be extended for more advanced parameters in the future.
- `LogUploadServerInfo` supports custom log upload server domain, path, port, and HTTPS settings.

## Utility Classes

### Constants

The `Constants` class contains various static constants and enumerations used throughout the Agora Recording SDK.

#### Main Enumerations

Below are some of the key enumerations defined in this class. Refer to the `Constants.java` source file or the detailed Javadoc for a complete list and descriptions of all members.

- **`AreaCode`**: Defines regions for server connection.
- **`ChannelProfileType`**: Defines channel profiles (Communication, Live Broadcast, etc.).
- **`AudioScenarioType`**: Defines audio application scenarios (Default, Game Streaming, Chatroom, etc.).
- **`EncryptionErrorType`**: Defines types of encryption errors.
- **`ErrorCodeType`**: Defines error codes returned by the SDK.
- **`LogLevel`**: Defines logging levels.
- **`EncryptionMode`**: Defines media stream encryption modes.
- **`VideoStreamType`**: Defines video stream types (High, Low).
- **`MediaRecorderContainerFormat`**: Defines recording file container formats (e.g., MP4).
- **`MediaRecorderStreamType`**: Defines what content to record (Audio, Video, Both).
- **`VideoSourceType`**: Defines the source of the video stream (Camera, Screen, etc.).
- **`WatermarkSourceType`**: Defines the type of watermark source (Literal, Timestamp, Picture).
- **`WatermarkFitMode`**: Defines how watermarks adapt to video dimensions.
- **`ConnectionChangedReasonType`**: Defines reasons for connection state changes.
- **`UserOfflineReasonType`**: Defines reasons why a remote user goes offline.
- **`RemoteVideoState`**: Defines states of remote video streams.
- **`RemoteVideoStateReason`**: Defines reasons for remote video state changes.
- **`RemoteAudioState`**: Defines states of remote audio streams.
- **`RemoteAudioStateReason`**: Defines reasons for remote audio state changes.
- **`RecorderState`**: Defines states of the media recorder.
- **`RecorderReasonCode`**: Defines reasons for recorder state changes.

### Utils

The `Utils` class provides static utility methods for common tasks.

#### Static Methods

- **`boolean isNullOrEmpty(String str)`**: Checks if a string is null or empty.
- **`void cleanDirectBuffer(ByteBuffer buffer)`**: Attempts to clean a direct ByteBuffer by setting it to null and invoking garbage collection.
- **`String bytesToHex(byte[] bytes)`**: Converts a byte array to a hexadecimal string.
- **`byte[] hexStringToByteArray(String s)`**: Converts a hexadecimal string to a byte array.
- **`byte[] byteStringToByteArray(String byteString)`**: Converts a string to a byte array where each character becomes a byte.
- **`String byteBufferToString(ByteBuffer buffer)`**: Converts the content of a ByteBuffer to a UTF-8 string.
- **`boolean areFilesIdentical(String file1Path, String file2Path)`**: Compares the content of two files to check if they are identical.
- **`void deleteAllFile(String filePath)`**: Deletes all files in the same directory as `filePath` that start with the same name as `filePath`.
- **`String readFile(String filePath)`**: Reads the entire content of a file into a string.
- **`String formatTimestamp(long timestamp, String pattern)`**: Formats a timestamp (in milliseconds) into a string using the specified pattern (e.g., "yyyy-MM-dd_HH-mm-ss").
