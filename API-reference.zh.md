# Agora Recording Java SDK API Reference

本文档详细描述了 Agora Recording Java SDK 的 API 接口，帮助开发者快速理解和使用 SDK 的各项功能。

## 目录

- [核心类](#core-classes)
  - [AgoraService](#agoraservice)
  - [AgoraMediaComponentFactory](#agoramediacomponentfactory)
  - [AgoraMediaRtcRecorder](#agoramediartcrecorder)
  - [AgoraParameter](#agoraparameter)
- [观察者接口](#observer-interfaces)
  - [IAgoraMediaRtcRecorderEventHandler](#iagoramediartcrecordereventhandler)
- [数据结构](#data-structures)
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
- [实用工具类](#utility-classes)
  - [Constants](#constants)
  - [Utils](#utils)

## 核心类

### AgoraService

`AgoraService` 类提供初始化和管理 Agora 服务的核心功能，是使用 Agora 录制功能的主要入口点。

#### 主要方法

##### `AgoraService()`

构造一个 `AgoraService` 实例并初始化原生组件。同一时间只能初始化一个 `AgoraService` 实例。

##### `int initialize(AgoraServiceConfiguration config)`

使用指定的配置初始化 `AgoraService` 对象。

**参数**:

- `config`: 包含初始化参数的 `AgoraServiceConfiguration` 对象

**返回值**:

- `0`: 成功
- `< 0`: 失败，具体错误码可能包括：
  - `ERR_INVALID_ARGUMENT (-2)`: 如果 `AgoraServiceConfiguration` 中的 `context` 未提供 (仅限 Android)。
  - `ERR_INIT_NET_ENGINE (-?)`: 如果网络引擎无法初始化 (例如，防火墙阻止)。

##### `AgoraMediaComponentFactory createAgoraMediaComponentFactory()`

创建并返回一个用于创建媒体组件的 `AgoraMediaComponentFactory` 对象。

**返回值**:

- 返回一个 `AgoraMediaComponentFactory` 实例

##### `AgoraParameter getAgoraParameter()`

创建并返回一个用于参数管理的 `AgoraParameter` 对象。

**返回值**:

- 成功时返回 `AgoraParameter` 实例
- 失败时返回 `null`

##### `int setLogFile(String filePath, int fileSize)`

设置 SDK 日志文件的路径和大小。SDK 将日志记录在两个文件中，每个文件默认大小为 512 KB。如果 `fileSize` 设置为 1024 KB，则日志的最大大小将为 2 MB。如果总大小超过限制，新日志将覆盖旧日志。

**注意：** 在 `initialize` 后立即调用此方法以确保日志完整。

**参数**:

- `filePath`: 日志文件的路径。确保目录存在且可写。
- `fileSize`: 每个 SDK 日志文件的大小（字节）。

**返回值**:

- `0`: 成功
- `< 0`: 失败

##### `int setLogLevel(Constants.LogLevel level)`

设置 SDK 日志输出的级别。将输出选定级别及以上级别的日志。级别顺序为 OFF, CRITICAL, ERROR, WARNING, INFO, DEBUG。

**参数**:

- `level`: 日志文件的级别。参见 {@link Constants.LogLevel}。
  - `LOG_LEVEL_NONE (0x0000)`: 无日志输出。
  - `LOG_LEVEL_INFO (0x0001)`: (推荐) 输出 INFO 级别及以上日志。
  - `LOG_LEVEL_WARN (0x0002)`: 输出 WARN 级别及以上日志。
  - `LOG_LEVEL_ERROR (0x0004)`: 输出 ERROR 级别及以上日志。
  - `LOG_LEVEL_FATAL (0x0008)`: 输出 FATAL 级别日志。

**返回值**:

- `0`: 成功
- `< 0`: 失败

##### `int release()`

释放 `AgoraService` 对象及其关联资源。调用此方法后，该实例将失效。

**返回值**:

- `0`: 成功
- `< 0`: 失败

##### `int setGlobalLocalAccessPoint(LocalAccessPointConfiguration config)`

设置本地 AP 模式下的全局本地接入点地址（也会调用本地代理）。

**注意：** 此方法必须在 AgoraService 的 `initialize(AgoraServiceConfiguration)` 之前调用。它会影响同一进程下的所有录制实例，每个进程只需调用一次。

**参数**：

- `config`: {@link LocalAccessPointConfiguration} 对象。详见 LocalAccessPointConfiguration 的定义。

**返回值**：

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

### AgoraMediaComponentFactory

`AgoraMediaComponentFactory` 类是用于创建 Agora 媒体组件的工厂类。该类提供创建媒体录制组件实例的功能。

#### 主要方法

##### `AgoraMediaRtcRecorder createMediaRtcRecorder()`

创建一个新的 `AgoraMediaRtcRecorder` 实例。

**返回值**:

- 一个新的 `AgoraMediaRtcRecorder` 实例

**异常**:

- `RuntimeException`: 如果原生录制器创建失败。

##### `int release()`

释放与工厂关联的本地资源。

**返回值**:

- `0`: 成功
- `< 0`: 失败

### AgoraMediaRtcRecorder

`AgoraMediaRtcRecorder` 类提供录制 Agora RTC 媒体流的功能。该类允许录制来自 Agora RTC 频道的音频和视频流，并提供流混合、加密和选择性订阅的选项。

#### 主要方法

##### `int initialize(AgoraService service, boolean enableMix)`

使用指定的服务和混合设置初始化录制器。

**参数**:

- `service`: 一个 Agora 服务实例，必须在调用此方法之前初始化。
- `enableMix`: 是否启用流混合。

**返回值**:

- `0`: 初始化成功。
- `< 0`: 初始化失败。

##### `int joinChannel(String token, String channelName, String userId)`

加入一个 Agora RTC 频道。

**参数**:

- `token`: 用于身份验证的令牌。
- `channelName`: 要加入的频道名称。不得超过 64 字节，可以包含小写/大写字母、数字、空格和特殊字符 `!#$%&()+,-.:;<=>?@[]^_{|}~`。
- `userId`: 本地用户的用户 ID。如果设置为 `null` 或 `"0"`，系统将自动分配一个。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int leaveChannel()`

离开当前频道。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int enableEncryption(boolean enabled, EncryptionConfig config)`

启用或禁用内置加密。建议在加入频道前调用以确保高安全性。同一频道中的所有用户必须使用相同的模式和密钥。当所有用户离开时，密钥将被清除。

**注意：** 启用加密会禁用 RTMP 推流。

**参数**:

- `enabled`: 是否启用内置加密。
- `config`: 加密配置参数。参见 {@link EncryptionConfig}。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int subscribeAllAudio()`

订阅频道中所有远程用户的音频流。自动订阅稍后加入的新用户。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int subscribeAllVideo(VideoSubscriptionOptions options)`

订阅频道中所有远程用户的视频流。自动订阅稍后加入的新用户。

**参数**:

- `options`: 视频订阅选项，包括流类型和其他参数。参见 {@link VideoSubscriptionOptions}。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int unsubscribeAllAudio()`

停止订阅所有远程用户的音频流。除非再次调用 `subscribeAudio` 或 `subscribeAllAudio`，否则自动停止订阅新用户。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int unsubscribeAllVideo()`

停止订阅所有远程用户的视频流。除非再次调用 `subscribeVideo` 或 `subscribeAllVideo`，否则自动停止订阅新用户。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int subscribeAudio(String userId)`

订阅指定远程用户的音频流。

**参数**:

- `userId`: 要订阅其音频的远程用户的 ID。

**返回值**:

- `0`: 方法调用成功。
- `-2`: 无效的 userId。
- 其他 `< 0`: 方法调用失败。

##### `int unsubscribeAudio(String userId)`

停止订阅指定远程用户的音频流。

**参数**:

- `userId`: 要取消订阅其音频的远程用户的 ID。

**返回值**:

- `0`: 方法调用成功。
- `-2`: 无效的 userId。
- 其他 `< 0`: 方法调用失败。

##### `int subscribeVideo(String userId, VideoSubscriptionOptions options)`

订阅指定远程用户的视频流。

**参数**:

- `userId`: 要订阅其视频的远程用户的 ID。
- `options`: 视频订阅选项。参见 {@link VideoSubscriptionOptions}。

**返回值**:

- `0`: 方法调用成功。
- `-2`: 无效的 userId。
- 其他 `< 0`: 方法调用失败。

##### `int unsubscribeVideo(String userId)`

停止订阅指定远程用户的视频流。

**参数**:

- `userId`: 要取消订阅其视频的远程用户的 ID。

**返回值**:

- `0`: 方法调用成功。
- `-2`: 无效的 userId。
- 其他 `< 0`: 方法调用失败。

##### `int setVideoMixingLayout(VideoMixingLayout layout)`

设置混合视频流的布局。

**参数**:

- `layout`: 混合视频流的布局配置。参见 {@link VideoMixingLayout}。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int setRecorderConfig(MediaRecorderConfiguration config)`

配置录制器设置。必须在开始录制之前调用此方法。

**参数**:

- `config`: 录制器配置参数。参见 {@link MediaRecorderConfiguration}。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int setRecorderConfigByUid(MediaRecorderConfiguration config, String userId)`

为指定用户配置录制器设置（用于单流录制）。

**参数**:

- `config`: 录制器配置参数。参见 {@link MediaRecorderConfiguration}。
- `userId`: 要配置录制设置的用户的用户 ID。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int startRecording()`

开始录制过程（混合流或先前配置的单流）。在调用此方法之前，请确保使用 `setRecorderConfig`（用于混合流）或 `setRecorderConfigByUid`（用于单流）配置录制器。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int stopRecording()`

停止混合流录制过程并保存录制的文件。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int startSingleRecordingByUid(String userId)`

开始为指定用户进行单流录制。确保已为此用户调用 `setRecorderConfigByUid`。

**参数**:

- `userId`: 要开始录制的用户的用户 ID。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int stopSingleRecordingByUid(String userId)`

停止为指定用户进行的单流录制过程并保存文件。

**参数**:

- `userId`: 要停止录制的用户的用户 ID。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int registerRecorderEventHandler(IAgoraMediaRtcRecorderEventHandler handler)`

注册录制事件的事件处理程序。

**参数**:

- `handler`: 实现 `IAgoraMediaRtcRecorderEventHandler` 接口的事件处理程序。

**返回值**:

- `0`: 注册成功。
- `< 0`: 注册失败。

##### `int unregisterRecorderEventHandle(IAgoraMediaRtcRecorderEventHandler handler)`

取消注册事件处理程序。

**参数**:

- `handler`: 先前注册的事件处理程序。

**返回值**:

- `0`: 取消注册成功。
- `< 0`: 取消注册失败。

##### `int enableAndUpdateVideoWatermarks(WatermarkConfig[] watermarkConfigs)`

为混合流视频添加或更新水印。

**参数**:

- `watermarkConfigs`: 水印配置。参见 {@link WatermarkConfig}。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int disableVideoWatermarks()`

禁用混合流视频的水印。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int enableAndUpdateVideoWatermarksByUid(WatermarkConfig[] watermarkConfigs, String userId)`

为特定用户的单流视频添加或更新水印。

**参数**:

- `watermarkConfigs`: 水印配置。参见 {@link WatermarkConfig}。
- `userId`: 应应用水印的视频流所属用户的用户 ID。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int disableVideoWatermarksByUid(String userId)`

禁用特定用户的单流视频的水印。

**参数**:

- `userId`: 应禁用其视频流水印的用户的用户 ID。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int setAudioVolumeIndicationParameters(int intervalInMs)`

设置 `onAudioVolumeIndication` 回调的间隔。

**参数**:

- `intervalInMs`: 间隔（毫秒）。默认为 500。
  - `<= 10`: 禁用回调。
  - `> 10`: 回调之间的间隔。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

##### `int renewToken(String token)`

更新当前会话的 Token。
Token 在一段时间后会过期。当 `IAgoraMediaRtcRecorderEventHandler#onError(String, Constants.ErrorCodeType, String)` 回调报告 `Constants.ErrorCodeType#ERR_TOKEN_EXPIRED (109)`，或者当 `IAgoraMediaRtcRecorderEventHandler#onTokenPrivilegeWillExpire(String, String)` 或 `IAgoraMediaRtcRecorderEventHandler#onTokenPrivilegeDidExpire(String)` 被触发时，你必须在你的服务器上生成一个新的 Token，并调用此方法来更新它。否则，SDK 将会与 Agora 频道断开连接。

**参数**:

- `token`: 从你的服务器生成的新 Token。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败 (例如，如果 Token 为 null 或空)。

##### `int release()`

释放录制器实例及相关资源。

**返回值**:

- `0`: 方法调用成功。
- `< 0`: 方法调用失败。

### AgoraParameter

`AgoraParameter` 类提供了一个高级参数设置接口，允许您在运行时访问和修改 SDK 的附加功能。它支持布尔、整数、无符号整数、双精度浮点数、字符串、对象 (JSON) 和数组 (JSON) 类型。

#### 主要方法

##### `int setBool(String key, boolean value)`

设置一个布尔参数。

**参数**:

- `key`: 参数名称。
- `value`: 参数值。

**返回值**:

- `0`: 设置成功。
- `< 0`: 设置失败。

##### `int setInt(String key, int value)`

设置一个整数参数。

**参数**:

- `key`: 参数名称。
- `value`: 参数值。

**返回值**:

- `0`: 设置成功。
- `< 0`: 设置失败。

##### `int setUInt(String key, int value)`

设置一个无符号整数参数。

**参数**:

- `key`: 参数名称。
- `value`: 参数值 (使用 Java `int`)。

**返回值**:

- `0`: 设置成功。
- `< 0`: 设置失败。

##### `int setNumber(String key, double value)`

设置一个浮点数参数。

**参数**:

- `key`: 参数名称。
- `value`: 参数值。

**返回值**:

- `0`: 设置成功。
- `< 0`: 设置失败。

##### `int setString(String key, String value)`

设置一个字符串参数。

**参数**:

- `key`: 参数名称。
- `value`: 参数值。

**返回值**:

- `0`: 设置成功。
- `< 0`: 设置失败。

##### `int setObject(String key, String value)`

使用 JSON 字符串设置对象参数值。

**参数**:

- `key`: 参数名称。
- `value`: 表示对象的 JSON 字符串。

**返回值**:

- `0`: 设置成功。
- `< 0`: 设置失败。

##### `int setArray(String key, String value)`

使用 JSON 字符串设置数组参数值。

**参数**:

- `key`: 参数名称。
- `value`: 表示数组的 JSON 字符串。

**返回值**:

- `0`: 设置成功。
- `< 0`: 设置失败。

##### `boolean getBool(String key)`

检索布尔参数值。

**参数**:

- `key`: 参数名称。

**返回值**:

- 与键关联的布尔值。

**异常**:

- `IllegalStateException`: 如果无法检索参数。

##### `int getInt(String key)`

检索整数参数值。

**参数**:

- `key`: 参数名称。

**返回值**:

- 与键关联的整数值。

**异常**:

- `IllegalStateException`: 如果无法检索参数。

##### `int getUInt(String key)`

检索无符号整数参数值。

**参数**:

- `key`: 参数名称。

**返回值**:

- 与键关联的无符号整数值 (作为 Java `int` 返回)。

**异常**:

- `IllegalStateException`: 如果无法检索参数。

##### `double getNumber(String key)`

检索浮点数参数值。

**参数**:

- `key`: 参数名称。

**返回值**:

- 与键关联的双精度浮点数值。

**异常**:

- `IllegalStateException`: 如果无法检索参数。

##### `String getString(String key)`

检索字符串参数值。

**参数**:

- `key`: 参数名称。

**返回值**:

- 与键关联的字符串值，如果未找到则为 `null`。

##### `String getObject(String key)`

以 JSON 字符串形式检索对象参数值。

**参数**:

- `key`: 参数名称。

**返回值**:

- 表示对象的 JSON 字符串，如果未找到则为 `null`。

##### `String getArray(String key, String args)`

以 JSON 字符串形式检索数组参数值。

**参数**:

- `key`: 参数名称。
- `args`: 用于数组检索的附加参数（具体用法可能不同）。

**返回值**:

- 表示数组的 JSON 字符串，如果未找到则为 `null`。

##### `int setParameters(String parameters)`

使用 JSON 字符串一次设置多个参数。

**参数**:

- `parameters`: 包含多个参数键值对的 JSON 字符串。

**返回值**:

- `0`: 设置成功。
- `< 0`: 设置失败。

##### `String convertPath(String filePath)`

将文件路径转换为特定于平台的格式。

**参数**:

- `filePath`: 要转换的原始文件路径。

**返回值**:

- 转换后的平台特定文件路径，如果转换失败则为 `null`。

##### `void release()`

释放与此参数实例关联的所有资源。此调用后实例将失效。

## 观察者接口

### IAgoraMediaRtcRecorderEventHandler

`IAgoraMediaRtcRecorderEventHandler` 接口定义了 SDK 与 Agora 频道之间连接状态变化的回调方法，以及其他与录制相关的事件回调。

#### 主要回调方法

##### `void onConnected(String channelId, String userId)`

当 SDK 与 Agora 频道的连接状态变为 `CONNECTION_STATE_CONNECTED(3)` 时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 用户 ID。

##### `void onDisconnected(String channelId, String userId, Constants.ConnectionChangedReasonType reason)`

当 SDK 与 Agora 频道的连接状态变为 `CONNECTION_STATE_DISCONNECTED(1)` 时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 用户 ID。
- `reason`: 连接状态改变的原因。参见 {@link Constants.ConnectionChangedReasonType}。

##### `void onReconnected(String channelId, String userId, Constants.ConnectionChangedReasonType reason)`

当连接断开后，连接状态再次变为 `CONNECTION_STATE_CONNECTED(3)` 时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 用户 ID。
- `reason`: 连接状态改变的原因。参见 {@link Constants.ConnectionChangedReasonType}。

##### `void onConnectionLost(String channelId, String userId)`

当 SDK 与 Agora 频道失去连接时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 用户 ID。

##### `void onUserJoined(String channelId, String userId)`

当远程用户加入频道时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 加入频道的远程用户的 ID。

##### `void onUserLeft(String channelId, String userId, Constants.UserOfflineReasonType reason)`

当远程用户离开频道时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 离开频道的远程用户的 ID。
- `reason`: 用户离开的原因。参见 {@link Constants.UserOfflineReasonType}。

##### `void onUserVideoStateChanged(String channelId, String userId, Constants.RemoteVideoState state, Constants.RemoteVideoStateReason reason, int elapsed)`

当远程用户视频流的状态发生改变时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 视频状态已改变的远程用户的 ID。
- `state`: 视频的当前状态。参见 {@link Constants.RemoteVideoState}。
- `reason`: 状态改变的原因。参见 {@link Constants.RemoteVideoStateReason}。
- `elapsed`: 从本地用户调用 `joinChannel` 到此事件发生所经过的时间（毫秒）。

##### `void onUserAudioStateChanged(String channelId, String userId, Constants.RemoteAudioState state, Constants.RemoteAudioStateReason reason, int elapsed)`

当远程用户音频流的状态发生改变时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 音频状态已改变的远程用户的 ID。
- `state`: 音频的当前状态。参见 {@link Constants.RemoteAudioState}。
- `reason`: 状态改变的原因。参见 {@link Constants.RemoteAudioStateReason}。
- `elapsed`: 从本地用户调用 `joinChannel` 到此事件发生所经过的时间（毫秒）。

##### `void onFirstRemoteVideoDecoded(String channelId, String userId, int width, int height, int elapsed)`

当 SDK 解码远程视频流的第一帧时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 远程用户 ID。
- `width`: 视频宽度（像素）。
- `height`: 视频高度（像素）。
- `elapsed`: 从本地用户调用 `joinChannel` 到此事件发生所经过的时间（毫秒）。

##### `void onFirstRemoteAudioDecoded(String channelId, String userId, int elapsed)`

当 SDK 解码远程音频流的第一帧用于播放时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 发送音频流的远程用户 ID。
- `elapsed`: 从本地用户调用 `joinChannel` 到此事件发生所经过的时间（毫秒）。

##### `void onAudioVolumeIndication(String channelId, SpeakVolumeInfo[] speakers, int speakerNumber)`

报告哪些用户正在讲话及其音量。

**参数**:

- `channelId`: 频道 ID。
- `speakers`: 每个发言者的音量信息数组。参见 {@link SpeakVolumeInfo}。
- `speakerNumber`: 发言者总数。

##### `void onActiveSpeaker(String channelId, String userId)`

当检测到活跃发言者（音量最高的用户）时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 活跃发言者的 ID。`userId` 为 `"0"` 表示本地用户。

##### `void onRemoteVideoStats(String channelId, String userId, RemoteVideoStatistics stats)`

报告远程视频流的统计信息。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 远程用户的 ID。
- `stats`: 视频的当前统计信息。参见 {@link RemoteVideoStatistics}。

##### `void onRemoteAudioStats(String channelId, String userId, RemoteAudioStatistics stats)`

报告远程音频流的统计信息。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 远程用户的 ID。
- `stats`: 音频的当前统计信息。参见 {@link RemoteAudioStatistics}。

##### `void onRecorderStateChanged(String channelId, String userId, Constants.RecorderState state, Constants.RecorderReasonCode reason, String fileName)`

当录制状态改变时触发。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 用户 ID。
- `state`: 当前录制状态。参见 {@link Constants.RecorderState}。
- `reason`: 录制状态改变的原因。参见 {@link Constants.RecorderReasonCode}。
- `fileName`: 录制文件的名称。

##### `void onRecorderInfoUpdated(String channelId, String userId, RecorderInfo info)`

根据 `MediaRecorderConfiguration` 中的 `recorderInfoUpdateInterval` 定期触发，报告更新的录制信息（文件名、时长、大小）。

**参数**:

- `channelId`: 频道 ID。
- `userId`: 用户 ID。
- `info`: 有关录制文件的信息。参见 {@link RecorderInfo}。

##### `void onEncryptionError(String channelId, Constants.EncryptionErrorType errorType)`

当加密过程中发生错误时触发。

**参数**:

- `channelId`: 频道 ID。
- `errorType`: 加密错误的类型。参见 {@link Constants.EncryptionErrorType}。

##### `void onError(String channelId, Constants.ErrorCodeType error, String message)`

报告错误码和错误消息。

**参数**:

- `channelId`: 频道 ID。
- `error`: 错误码。详见 {@link io.agora.recording.Constants.ErrorCodeType ErrorCode}。
- `message`: 错误消息。

##### `void onTokenPrivilegeWillExpire(String channelId, String token)`

Token 即将在 30 秒内过期时触发。

SDK 触发此回调以提醒应用在 Token 权限过期之前获取新 Token。

<p>
收到此回调后，你必须在你的服务器上生成一个新的 Token，并调用
{@link AgoraMediaRtcRecorder#renewToken(String)} 来更新它。

**参数**:

- `channelId`: 频道 ID。
- `token`: 即将在 30 秒内过期的 Token。

##### `void onTokenPrivilegeDidExpire(String channelId)`

Token 已过期时触发。

收到此回调后，你必须在你的服务器上生成一个新的 Token，并调用
{@link AgoraMediaRtcRecorder#renewToken(String)} 来更新它。

**参数**:

- `channelId`: 频道 ID。

## 数据结构

### AgoraServiceConfiguration

`AgoraServiceConfiguration` 类用于配置和初始化 Agora 服务实例。此类包含初始化和配置 Agora 服务实例所需的所有设置。

#### 主要属性

- **enableAudioProcessor**: 是否启用音频处理模块。默认值：`true`。禁用将阻止创建音轨。
- **enableAudioDevice**: 是否启用音频设备模块（用于管理音频录制/播放设备）。默认值：`true`。禁用将阻止通过设备进行音频录制/播放，但如果 `enableAudioProcessor` 为 true，仍可以推送 PCM 数据。
- **enableVideo**: 是否启用视频。默认值：`false`。
- **context**: 用户上下文。对于 Android，它是 Activity 的 `Context`。(仅限 Android)。
- **appId**: 项目的 App ID。
- **areaCode**: 支持的区域代码。默认值：`AREA_CODE_GLOB`。参见 {@link Constants.AreaCode}。
- **channelProfile**: 频道配置文件。默认值：`CHANNEL_PROFILE_LIVE_BROADCASTING`。参见 {@link Constants.ChannelProfileType}。
- **license**: 连接频道时用于验证的许可证。
- **audioScenario**: 音频场景。默认值：`AUDIO_SCENARIO_DEFAULT`。参见 {@link Constants.AudioScenarioType}。
- **logConfig**: 用户定义的日志配置。参见 {@link LogConfig}。
- **useStringUid**: 是否启用字符串用户 ID。默认值：`false`。
- **useExternalEglContext**: 是否使用当前线程中的 EGL 上下文作为 SDK 的根 EGL 上下文。(仅限 Android)。默认值：`false`。
- **domainLimit**: 是否启用域名限制（仅连接到由 DNS 解析的服务器）。默认值：`false`。

### MediaRecorderConfiguration

`MediaRecorderConfiguration` 类用于配置录制的视频和音频参数。它包括存储路径、容器格式、流类型、最大时长和更新间隔等设置。

#### 主要属性

- **storagePath**: 录制文件的绝对路径（包括文件名和扩展名）。确保目录存在且可写。示例路径：
  - Windows: `C:/Users/userName/AppData/Local/Agora/process_name/example.mp4`
  - Linux: `result/example.mp4`
  - Android: `/storage/emulated/0/Android/data/package_name/files/example.mp4`
  - macOS: `/Library/Logs/example.mp4`
  - iOS: `/App Sandbox/Library/Caches/example.mp4`
- **containerFormat**: 录制文件的格式。默认值：`FORMAT_MP4`。参见 {@link Constants.MediaRecorderContainerFormat}。
- **streamType**: 录制内容。默认值：`STREAM_TYPE_BOTH`。参见 {@link Constants.MediaRecorderStreamType}。
- **maxDurationMs**: 最大录制时长（毫秒）。默认值：`120000`。
- **recorderInfoUpdateInterval**: `onRecorderInfoUpdated` 回调的间隔（毫秒）。范围：[1000, 10000]。默认值：`0`（禁用）。
- **width**: 录制视频的宽度（像素）。默认值：`1280`。
- **height**: 录制视频的高度（像素）。默认值：`720`。
- **fps**: 录制视频的帧率 (fps)。默认值：`30`。
- **sampleRate**: 录制音频的采样率 (Hz)。默认值：`48000`。
- **channelNum**: 音频通道数。默认值：`1`（单声道）。
- **videoSourceType**: 用于录制的视频源类型。默认值：`VIDEO_SOURCE_CAMERA_PRIMARY`。参见 {@link Constants.VideoSourceType}。

### EncryptionConfig

`EncryptionConfig` 类用于配置媒体流加密选项。

#### 主要属性

- **encryptionMode**: 加密模式。默认值：`AES_128_GCM2`。参见 {@link Constants.EncryptionMode}。
- **encryptionKey**: 字符串格式的加密密钥。设置为 `null` 或不设置将禁用加密（返回 `ERR_INVALID_ARGUMENT`）。
- **encryptionKdfSalt**: 加密盐值（字节数组）。默认值：32 个零。设置时必须为 32 字节长。

### VideoSubscriptionOptions

`VideoSubscriptionOptions` 类用于配置视频订阅选项。

#### 主要属性

- **type**: 要订阅的视频流类型。默认值：`VIDEO_STREAM_HIGH`。参见 {@link Constants.VideoStreamType}。
- **encodedFrameOnly**: 是否只接收编码帧。默认值：`false`。

### VideoMixingLayout

`VideoMixingLayout` 类用于配置混合视频的布局。它包括画布尺寸、帧率、背景颜色/图像以及用户布局配置。

#### 主要属性

- **canvasWidth**: 画布宽度（像素）。
- **canvasHeight**: 画布高度（像素）。
- **canvasFps**: 画布（输出混合视频）的帧率。默认值：0。
- **backgroundColor**: 画布的背景颜色（长整型值，例如，红色为 0xFF0000）。默认值：0。
- **backgroundImage**: 画布的背景图像 URL。优先于 `backgroundColor`。默认值：`null`。
- **userLayoutConfigs**: 混合中每个用户的布局配置数组。参见 {@link UserMixerLayout}。默认值：`null`。

### MixerLayoutConfig

`MixerLayoutConfig` 类提供混音器布局的配置。

#### 主要属性

- **x**: 左上角的 X 坐标。默认值：`0`。
- **y**: 左上角的 Y 坐标。默认值：`0`。
- **width**: 布局宽度。默认值：`0`。
- **height**: 布局高度。默认值：`0`。
- **zOrder**: 显示层级。值越高，绘制在越上层。默认值：`0`。
- **alpha**: 透明度级别 (0.0: 完全透明, 1.0: 完全不透明)。默认值：`1.0f`。
- **mirror**: 布局是否水平镜像。默认值：`false`。
- **imagePath**: 布局背景图像的文件路径。默认值：`null`。
- **rotation**: 旋转角度 (0: 无, 1: 90, 2: 180, 3: 270 度)。默认值：`0`。

### UserMixerLayout

`UserMixerLayout` 类表示混合流中特定用户的布局配置。它将用户 ID 与其布局设置相关联。

#### 主要属性

- **userId**: 用户的 ID。
- **config**: 此用户的布局配置设置。参见 {@link MixerLayoutConfig}。

### WatermarkConfig

`WatermarkConfig` 类用于配置水印选项。

#### 主要属性

- **index**: 水印索引。默认值：`0`。
- **type**: 水印源类型。默认值：`WatermarkSourceType.PICTURE`。参见 {@link Constants.WatermarkSourceType}。
- **timestampSource**: 时间戳水印配置。参见 {@link WatermarkTimestamp}。默认值：`null`。
- **literaSource**: 文本（字面量）水印配置。参见 {@link WatermarkLitera}。默认值：`null`。
- **imageUrl**: 图像水印 URL。默认值：`null`。
- **options**: 水印显示选项。参见 {@link WatermarkOptions}。默认值：`null`。

### WatermarkOptions

`WatermarkOptions` 类用于配置水印显示选项。

#### 主要属性

- **visibleInPreview**: 水印图像是否在本地视频预览中可见。默认值：`true`。
- **mode**: 水印适配模式。默认值：`FIT_MODE_COVER_POSITION`。参见 {@link Constants.WatermarkFitMode}。
- **positionInLandscapeMode**: 当 `mode` 为 `FIT_MODE_COVER_POSITION` 时，用于设置横屏模式下的水印区域。参见 {@link Rectangle}。默认值：(0, 0, 0, 0)。
- **positionInPortraitMode**: 当 `mode` 为 `FIT_MODE_COVER_POSITION` 时，用于设置竖屏模式下的水印区域。参见 {@link Rectangle}。默认值：(0, 0, 0, 0)。
- **watermarkRatio**: 当 `mode` 为 `FIT_MODE_USE_IMAGE_RATIO` 时，用于设置水印坐标。参见 {@link WatermarkRatio}。默认值：默认的 `WatermarkRatio` 对象。
- **zOrder**: 水印显示层级顺序。默认值：`0`。

### WatermarkLitera

`WatermarkLitera` 类用于配置文本（字面量）水印选项。

#### 主要属性

- **wmLitera**: 水印文本内容。默认值：`null`。
- **fontFilePath**: 字体文件路径。默认值：`null`（使用系统字体）。
- **fontSize**: 字体大小。默认值：`10`。

### WatermarkTimestamp

`WatermarkTimestamp` 类用于配置时间戳水印选项。

#### 主要属性

- **fontSize**: 字体大小。默认值：`10`。
- **fontFilePath**: 字体文件路径。默认值：`null`（使用系统字体）。

### WatermarkRatio

`WatermarkRatio` 类使用相对于视频画布的比例来定义水印的位置和大小。

#### 主要属性

- **xRatio**: 左上角的水平位置比例 (0.0-1.0)。默认值：`0.0f`。
- **yRatio**: 左上角的垂直位置比例 (0.0-1.0)。默认值：`0.0f`。
- **widthRatio**: 相对于画布宽度的宽度比例 (0.0-1.0)。默认值：`0.0f`。

### LogConfig

`LogConfig` 类提供日志记录的配置。

#### 主要属性

- **filePath**: 日志文件路径。默认值：`null`（使用默认 SDK 日志路径）。
- **fileSizeInKB**: 每个日志文件的大小（KB）。默认值：`1024`。
- **level**: 日志级别。默认值：`LOG_LEVEL_INFO`。参见 {@link Constants.LogLevel}。

### Rectangle

`Rectangle` 类用于配置矩形选项。

#### 主要属性

- **x**: 左上角的 X 坐标。
- **y**: 左上角的 Y 坐标。
- **width**: 矩形的宽度。
- **height**: 矩形的高度。

### SpeakVolumeInfo

`SpeakVolumeInfo` 类包含发言者的音量信息。

#### 主要属性

- **userId**: 发言者的用户 ID。
- **volume**: 发言者的音量 (0-255)。

### RemoteVideoStatistics

`RemoteVideoStatistics` 类提供远程视频流的统计信息。

#### 主要属性

- **delay**: 延迟（毫秒）。
- **width**: 视频帧的宽度（像素）。
- **height**: 视频帧的高度（像素）。
- **receivedBitrate**: 接收到的比特率 (Kbps)。
- **decoderOutputFrameRate**: 解码视频输出的帧率。
- **rxStreamType**: 接收到的视频流类型。参见 {@link Constants.VideoStreamType}。

### RemoteAudioStatistics

`RemoteAudioStatistics` 类包含远程音频流的统计信息。

#### 主要属性

- **quality**: 音频流质量 (0: 未知, 1: 极好, 2: 好, 3: 差, 4: 糟糕, 5: 非常糟糕)。
- **networkTransportDelay**: 网络传输延迟（毫秒）。
- **jitterBufferDelay**: 抖动缓冲延迟（毫秒）。
- **audioLossRate**: 音频丢包率（百分比）。

### RecorderInfo

`RecorderInfo` 类包含录制信息。

#### 主要属性

- **fileName**: 录制文件的绝对路径。
- **durationMs**: 录制时长（毫秒）。
- **fileSize**: 录制文件的大小（字节）。

### AdvancedConfigInfo

`AdvancedConfigInfo` 类用于高级配置，目前主要用于日志上传服务器设置。

#### 主要属性

- **logUploadServer**: 日志上传服务器配置。参见 {@link LogUploadServerInfo}。

#### 主要方法

- `getLogUploadServer()`: 获取日志上传服务器配置。
- `setLogUploadServer(LogUploadServerInfo logUploadServer)`: 设置日志上传服务器配置。

### LogUploadServerInfo

`LogUploadServerInfo` 类描述日志上传服务器的信息。

#### 主要属性

- **serverDomain**: 日志上传服务器域名。
- **serverPath**: 日志上传服务器路径。
- **serverPort**: 日志上传服务器端口。
- **serverHttps**: 是否使用 HTTPS 请求（`true` 为 HTTPS，`false` 为 HTTP）。

#### 主要方法

- `getServerDomain()/setServerDomain(String)`: 获取/设置服务器域名。
- `getServerPath()/setServerPath(String)`: 获取/设置服务器路径。
- `getServerPort()/setServerPort(int)`: 获取/设置服务器端口。
- `isServerHttps()/setServerHttps(boolean)`: 获取/设置是否使用 HTTPS。

### LocalAccessPointConfiguration

`LocalAccessPointConfiguration` 类用于本地代理接入点的配置。

#### 主要属性

- **ipList**: 本地接入点 IP 地址列表。
- **ipListSize**: IP 地址数量。
- **domainList**: 本地接入点域名列表。
- **domainListSize**: 域名数量。
- **verifyDomainName**: 安装在特定本地接入点的证书域名。空字符串表示使用 SNI 域名。
- **mode**: 代理连接模式。参见 {@link Constants.LocalProxyMode}。
- **advancedConfig**: 高级配置。参见 {@link AdvancedConfigInfo}。
- **disableAut**: 是否禁用 vos-aut（默认：`true`）。

#### 主要方法

- `getIpList()/setIpList(String[])`: 获取/设置 IP 地址列表。
- `getIpListSize()/setIpListSize(int)`: 获取/设置 IP 数量。
- `getDomainList()/setDomainList(String[])`: 获取/设置域名列表。
- `getDomainListSize()/setDomainListSize(int)`: 获取/设置域名数量。
- `getVerifyDomainName()/setVerifyDomainName(String)`: 获取/设置证书域名。
- `getMode()/setMode(Constants.LocalProxyMode)`: 获取/设置代理连接模式。
- `getAdvancedConfig()/setAdvancedConfig(AdvancedConfigInfo)`: 获取/设置高级配置。
- `isDisableAut()/setDisableAut(boolean)`: 获取/设置是否禁用 vos-aut。

#### 使用说明

- `LocalAccessPointConfiguration` 可用于 `AgoraService#setGlobalLocalAccessPoint` 方法，影响同一进程下所有录制实例。
- `AdvancedConfigInfo` 目前主要用于日志上传服务器配置，后续可扩展更多高级参数。
- `LogUploadServerInfo` 支持自定义日志上传服务器的域名、路径、端口及 HTTPS 设置。

## 实用工具类

### Constants

`Constants` 类包含整个 Agora Recording SDK 中使用的各种静态常量和枚举。

#### 主要枚举

以下是此类中定义的一些关键枚举。有关所有成员的完整列表和描述，请参阅 `Constants.java` 源文件或详细的 Javadoc。

- **`AreaCode`**: 定义服务器连接的区域。
- **`ChannelProfileType`**: 定义频道配置文件（通信、直播等）。
- **`AudioScenarioType`**: 定义音频应用场景（默认、游戏流媒体、聊天室等）。
- **`EncryptionErrorType`**: 定义加密错误的类型。
- **`ErrorCodeType`**: 定义 SDK 返回的错误代码。
- **`LogLevel`**: 定义日志记录级别。
- **`EncryptionMode`**: 定义媒体流加密模式。
- **`VideoStreamType`**: 定义视频流类型（高、低）。
- **`MediaRecorderContainerFormat`**: 定义录制文件容器格式（例如 MP4）。
- **`MediaRecorderStreamType`**: 定义要录制的内容（音频、视频、两者）。
- **`VideoSourceType`**: 定义视频流的来源（摄像头、屏幕等）。
- **`WatermarkSourceType`**: 定义水印源的类型（字面量、时间戳、图片）。
- **`WatermarkFitMode`**: 定义水印如何适应视频尺寸。
- **`ConnectionChangedReasonType`**: 定义连接状态更改的原因。
- **`UserOfflineReasonType`**: 定义远程用户离线的原因。
- **`RemoteVideoState`**: 定义远程视频流的状态。
- **`RemoteVideoStateReason`**: 定义远程视频状态更改的原因。
- **`RemoteAudioState`**: 定义远程音频流的状态。
- **`RemoteAudioStateReason`**: 定义远程音频状态更改的原因。
- **`RecorderState`**: 定义媒体录制器的状态。
- **`RecorderReasonCode`**: 定义录制器状态更改的原因。

### Utils

`Utils` 类提供用于常见任务的静态实用方法。

#### 静态方法

- **`boolean isNullOrEmpty(String str)`**: 检查字符串是否为 null 或空。
- **`void cleanDirectBuffer(ByteBuffer buffer)`**: 尝试通过将直接 ByteBuffer 设置为 null 并调用垃圾回收来清理它。
- **`String bytesToHex(byte[] bytes)`**: 将字节数组转换为十六进制字符串。
- **`byte[] hexStringToByteArray(String s)`**: 将十六进制字符串转换为字节数组。
- **`byte[] byteStringToByteArray(String byteString)`**: 将字符串转换为字节数组，其中每个字符成为一个字节。
- **`String byteBufferToString(ByteBuffer buffer)`**: 将 ByteBuffer 的内容转换为 UTF-8 字符串。
- **`boolean areFilesIdentical(String file1Path, String file2Path)`**: 比较两个文件的内容以检查它们是否相同。
- **`void deleteAllFile(String filePath)`**: 删除与 `filePath` 同目录下且名称以 `filePath` 开头的所有文件。
- **`String readFile(String filePath)`**: 将文件的全部内容读入字符串。
- **`String formatTimestamp(long timestamp, String pattern)`**: 使用指定的模式（例如 "yyyy-MM-dd_HH-mm-ss"）将时间戳（毫秒）格式化为字符串。
