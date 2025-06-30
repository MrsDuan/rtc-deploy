# Agora Recording Java SDK

中文 | [English](./README.md)

## 目录

1. [简介](#简介)
2. [开发环境要求](#开发环境要求)
   - [硬件环境](#硬件环境)
   - [网络要求](#网络要求)
   - [带宽需求](#带宽需求)
   - [软件环境](#软件环境)
3. [SDK 下载](#SDK下载)
4. [集成 SDK](#集成SDK)
   - [1. Maven 集成](#1-maven-集成)
   - [2. 本地 SDK 集成](#2-本地-sdk-集成)
   - [3. 加载原生库 (.so 文件)](#3-加载原生库-so-文件)
     - [3.1 提取 so 库文件](#31-提取-so-库文件)
     - [3.2 配置加载路径](#32-配置加载路径)
5. [快速开始](#快速开始)
   - [开通服务](#开通服务)
   - [使用命令行录制](#使用命令行录制)
     - [前提条件](#前提条件)
     - [编译](#编译)
     - [设置录制选项](#设置录制选项)
     - [开始录制](#开始录制)
     - [结束录制](#结束录制)
   - [调用 API 录制](#调用-api-录制)
     - [前提条件](#前提条件-1)
     - [调用 API 实现录制](#调用-api-实现录制)
       - [初始化服务](#初始化服务)
       - [加入频道](#加入频道)
       - [开始录制](#开始录制-1)
       - [结束录制](#结束录制)
   - [跑通 Maven 工程](#跑通-maven-工程)
6. [API 参考](#api-参考)
7. [更新日志](#更新日志)
8. [其他参考](#其他参考)

## 简介

Agora Recording Java SDK (v4.4.150.4) 为您提供了强大的实时音视频录制能力，可无缝集成到 Linux 服务器端的 Java 应用程序中。借助此 SDK，您的服务器可以作为一个哑客户端加入 Agora 频道，实时拉取、订阅和录制频道内的音视频流。录制文件可用于内容存档、审核、分析或其他业务相关的高级功能。

## 开发环境要求

### 硬件环境

- **操作系统**：Ubuntu 14.04+ 或 CentOS 6.5+（推荐 7.0）
- **CPU 架构**：x86-64，arm64

### 网络要求

- **公网 IP**
- **域名访问**：允许访问 `.agora.io` 和 `.agoralab.co`

### 带宽需求

根据需要同时录制的频道数量和频道内情况确定所需带宽。以下数据可供参考：

- 录制一个分辨率为 640 × 480 的画面需要的带宽约为 500 Kbps
- 录制一个有两个人的频道则需 1 Mbps
- 同时录制 100 个这样的频道，需要带宽为 100 Mbps

### 软件环境

- **构建工具**：Apache Maven 或其他构建工具
- **JDK**：JDK 8+

## SDK 下载

### Maven 下载

#### x86_64 平台

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150.4</version>
</dependency>
```

#### arm64 平台

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150-aarch64</version>
</dependency>
```

### CDN 下载

#### x86_64 平台

[Agora-Linux-Recording-Java-SDK-v4.4.150.4-x86_64-738709-c4b18ea837-20250611_162648](https://download.agora.io/sdk/release/Agora-Linux-Recording-Java-SDK-v4.4.150.4-x86_64-738709-c4b18ea837-20250611_162648.zip)

#### arm64 平台

[Agora-Linux-Recording-Java-SDK-v4.4.150-aarch64-565361-c502888569-20250213_112934](https://download.agora.io/sdk/release/Agora-Linux-Recording-Java-SDK-v4.4.150-aarch64-565361-c502888569-20250213_112934.jar)

## 集成 SDK

SDK 集成有两种方式：通过 Maven 集成和本地 SDK 集成。

### 1. Maven 集成

Maven 集成是最简单的方式，可以自动管理 Java 依赖关系。

#### 1.1 添加 Maven 依赖

在项目的 `pom.xml` 文件中添加以下依赖：

```xml
<!-- x86_64 平台 -->
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150.4</version>
</dependency>

<!-- arm64 平台 -->
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150-aarch64</version>
</dependency>
```

#### 1.2 集成 so 库文件

Maven 依赖包含了所需的 JAR 文件，但仍需手动处理 `.so` 库文件才能运行。请参考下面的 **加载原生库 (.so 文件)** 部分。

### 2. 本地 SDK 集成

本地 SDK 是一个包含所有必要文件的完整包，适合需要更灵活控制的场景。

#### 2.1 SDK 包结构

从官网下载的 SDK 包（zip 格式）包含以下内容：

- **doc/** - JavaDoc 文档，详细的 API 说明
- **examples/** - 示例代码和项目
- **sdk/** - 核心 SDK 文件
  - `agora-recording-sdk.jar` - Java 类库
  - `agora-recording-sdk-javadoc.jar` - JavaDoc 文档

#### 2.2 集成 JAR 文件

你可以通过两种方式集成 JAR 文件：

###### 本地 Maven 仓库方法

方法一：只安装 SDK JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-recording-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-recording-java-sdk \
  -Dversion=4.4.150.4 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

方法二：同时安装 SDK JAR 和 JavaDoc JAR

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

安装后，在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-recording-java-sdk</artifactId>
    <version>4.4.150.4</version>
</dependency>
```

###### 直接引用方法

1. 将 JAR 文件复制到项目的 `libs` 目录：

   ```sh
   mkdir -p libs
   cp sdk/agora-recording-sdk.jar libs/
   cp sdk/agora-recording-sdk-javadoc.jar libs/  # 可选，用于 IDE 支持
   ```

2. 在 Java 项目中添加 classpath 引用：

   ```sh
   # 使用 SDK JAR
   java -cp .:libs/agora-recording-sdk.jar 你的主类

   # 在 IDE 中配置 JavaDoc（常见的 IDE 如 IntelliJ IDEA 或 Eclipse 支持直接关联 JavaDoc JAR）
   ```

#### 2.3 集成 so 库文件

下载的 SDK 包中已经包含了 `.so` 文件。你需要确保 Java 程序运行时能够找到这些文件。请参考下面的 **加载原生库 (.so 文件)** 部分。

### 加载原生库 (.so 文件)

Agora Linux Recording Java SDK 依赖于底层的 C++ 原生库（`.so` 文件）。无论是通过 Maven 集成还是本地集成，都需要确保 Java 虚拟机 (JVM) 在运行时能够找到并加载这些库。

#### 3.1 提取 so 库文件

`.so` 文件包含在 `agora-recording-sdk.jar` 或 `linux-recording-java-sdk-x.x.x.x.jar` 文件内部。你需要先将它们提取出来：

1.  在你的项目或部署目录下创建一个用于存放库文件的目录，例如 `libs`：

    ```sh
    mkdir -p libs
    cd libs
    ```

2.  使用 `jar` 命令从 SDK 的 JAR 文件中提取内容（假设 JAR 文件位于 `libs` 目录下或 Maven 缓存中）：

    ```sh
    # 如果使用本地集成方式，JAR 文件通常在 libs 目录下
    jar xvf agora-recording-sdk.jar

    # 如果使用 Maven 集成方式，JAR 文件在 Maven 缓存中，例如：
    # jar xvf ~/.m2/repository/io/agora/rtc/linux-recording-java-sdk/4.4.150.4/linux-recording-java-sdk-4.4.150.4.jar
    ```

3.  提取后，`libs` 目录下会生成 `native/linux/x86_64` 子目录，其中包含所需的 `.so` 文件：

    ```
    libs/
    ├── agora-recording-sdk.jar (或者空的，如果仅用于提取)
    ├── io/          # Java 的 class 类所在，无需关注
    ├── META-INF/    # JAR 文件和应用程序相关的元数据，无需关注
    └── native/      # 对应平台的 so 库文件
        └── linux/
            └── x86_64/   # x86_64 平台 so 库
                ├── libagora_rtc_sdk.so
                ├── libagora-fdkaac.so
                ├── libaosl.so
                └── librecording.so
    ```

#### 3.2 配置加载路径

有两种主要方法让 JVM 找到 `.so` 文件：

**方法一：通过设置环境变量 `LD_LIBRARY_PATH` (推荐)**

这是最可靠的方式，特别是在 `.so` 文件之间存在依赖关系时。

```sh
# 确定你的 .so 文件所在的目录，假设在 ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# 设置 LD_LIBRARY_PATH 环境变量，将库目录添加到现有路径的前面
export LD_LIBRARY_PATH=$LIB_DIR:$LD_LIBRARY_PATH

# 运行你的 Java 应用
java -jar 你的应用.jar
# 或者使用 classpath
# java -cp "你的classpath" 你的主类
```

**方法二：通过 JVM 参数 `-Djava.library.path`**

这种方法直接告诉 JVM 在哪里查找库文件。

```sh
# 确定你的 .so 文件所在的目录，假设在 ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# 运行 Java 应用，并通过 -D 参数指定库路径
java -Djava.library.path=$LIB_DIR -jar 你的应用.jar
# 或者使用 classpath
# java -Djava.library.path=$LIB_DIR -cp "你的classpath" 你的主类
```

> **注意**：
>
> - 推荐使用方法一 (`LD_LIBRARY_PATH`)，因为它能更好地处理库之间的依赖。如果仅使用 `-Djava.library.path`，有时可能因为库找不到其依赖的其他库而加载失败。
> - 确保 `$LIB_DIR` 指向包含 `libagora_rtc_sdk.so` 等文件的 **确切目录**。
> - 你可以将设置环境变量的命令放入启动脚本中，以便每次运行应用时自动配置。

参考以下脚本示例，它结合了两种方法，并设置了 classpath：

```sh
#!/bin/bash
# 获取当前脚本所在目录的绝对路径
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 确定 so 库文件路径 (假设在脚本目录下的 libs/native/linux/x86_64)
LIB_PATH="$SCRIPT_DIR/libs/native/linux/x86_64"
# SDK JAR 路径 (假设在脚本目录下的 libs)
SDK_JAR="$SCRIPT_DIR/libs/agora-recording-sdk.jar"
# 你的应用主类
MAIN_CLASS="你的主类"
# 你的应用的其他依赖 classpath (如果有)
APP_CP="你的其他classpath"

# 设置库路径环境变量
export LD_LIBRARY_PATH=$LIB_PATH:$LD_LIBRARY_PATH

# 组合 classpath
CLASSPATH=".:$SDK_JAR:$APP_CP" # '.' 表示当前目录

# 执行 Java 程序
# 同时使用 LD_LIBRARY_PATH 和 -Djava.library.path 以确保兼容性
java -Djava.library.path=$LIB_PATH -cp "$CLASSPATH" $MAIN_CLASS
```

## 快速开始

### 开通服务

参考 [官网开通服务](https://doc.shengwang.cn/doc/recording/java/get-started/enable-service)

### 使用命令行录制

#### 前提条件

开始前请确保你已经完成录制 SDK 的环境准备和集成工作。

> **注意**：当录制 SDK 加入频道时，相当于一个哑客户端加入频道，因此需要跟声网 RTC SDK 加入相同的频道，并使用相同的 App ID 和频道场景。

#### 编译示例项目

在 `Examples-Cmd` 目录下执行编译脚本：

```sh
./build.sh
```

#### 配置录制参数

录制参数使用 JSON 格式配置，位于 `Examples-Cmd/config` 目录下。

1. 查看配置示例：

   ```sh
   cat config/recorder_json.example
   ```

2. 创建或修改你自己的配置文件，例如 `config/my_recorder.json`，确保 JSON 格式正确。

3. 完整参数说明：

   | 参数名                   | 类型     | 说明                                                                                                                                    |
   | ------------------------ | -------- | --------------------------------------------------------------------------------------------------------------------------------------- |
   | appId                    | String   | 项目的 App ID，需要和 RTC SDK 中的 App ID 一致                                                                                          |
   | token                    | String   | 频道的 Token，如果频道设置了安全模式，需要传入 Token                                                                                    |
   | channelName              | String   | 频道名称，需要和 RTC SDK 中的频道名称一致                                                                                               |
   | useStringUid             | Boolean  | 是否使用字符串类型的用户 ID                                                                                                             |
   | useCloudProxy            | Boolean  | 是否使用云代理服务                                                                                                                      |
   | userId                   | String   | 用户 ID                                                                                                                                 |
   | subAllAudio              | Boolean  | 是否订阅所有音频。如果为 false，需要在 subAudioUserList 中填入订阅的用户 ID                                                             |
   | subAudioUserList         | String[] | 订阅音频的用户 ID 列表，仅在 subAllAudio 为 false 时生效                                                                                |
   | subAllVideo              | Boolean  | 是否订阅所有视频。如果为 false，需要在 subVideoUserList 中填入订阅的用户 ID                                                             |
   | subVideoUserList         | String[] | 订阅视频的用户 ID 列表，仅在 subAllVideo 为 false 时生效                                                                                |
   | subStreamType            | String   | 订阅的流类型，支持 `high`（大流）和 `low`（小流）                                                                                       |
   | isMix                    | Boolean  | 是否合流录制                                                                                                                            |
   | backgroundColor          | Long     | 合流录制的背景颜色。使用 RGB 颜色格式（0xRRGGBB），需要转为 long 类型的值。例如：红色为 0xFF0000，绿色为 0x00FF00，蓝色为 0x0000FF      |
   | backgroundImage          | String   | 合流录制的背景图片路径，支持 PNG 和 JPG 格式。当同时设置了背景颜色和背景图片时，背景图片优先生效                                        |
   | layoutMode               | String   | 合流录制布局模式，支持 `default`（默认布局），`bestfit`（自适应布局），`vertical`（垂直布局）                                           |
   | maxResolutionUid         | String   | 在 vertical 布局中，设定显示最大分辨率的用户 ID                                                                                         |
   | recorderStreamType       | String   | 录制类型，支持 `audio_only`（只录音频），`video_only`（只录视频），`both`（音视频都录）                                                 |
   | recorderPath             | String   | 录制文件路径。合流录制时为录制的文件名；单流录制时为录制的目录，以每一个用户 ID 为名的 mp4 文件                                         |
   | maxDuration              | Integer  | 录制时长，单位秒                                                                                                                        |
   | recoverFile              | Boolean  | 是否在录制时同时写 h264 和 aac 文件，程序 crash 后可以恢复出 mp4                                                                        |
   | audio                    | Object   | 音频设置                                                                                                                                |
   | audio.sampleRate         | Integer  | 音频采样率                                                                                                                              |
   | audio.numOfChannels      | Integer  | 音频通道数量                                                                                                                            |
   | video                    | Object   | 视频设置                                                                                                                                |
   | video.width              | Integer  | 视频宽度                                                                                                                                |
   | video.height             | Integer  | 视频高度                                                                                                                                |
   | video.fps                | Integer  | 视频帧率                                                                                                                                |
   | waterMark                | Object[] | 水印设置                                                                                                                                |
   | waterMark[].type         | String   | 水印类型，支持 `litera`（字幕水印），`time`（时间戳水印），`picture`（图片水印）                                                        |
   | waterMark[].litera       | String   | 字幕内容，仅在 type 为 `litera` 时生效                                                                                                  |
   | waterMark[].fontFilePath | String   | 字体文件路径                                                                                                                            |
   | waterMark[].fontSize     | Integer  | 字体大小                                                                                                                                |
   | waterMark[].x            | Integer  | 水印的 X 坐标                                                                                                                           |
   | waterMark[].y            | Integer  | 水印的 Y 坐标                                                                                                                           |
   | waterMark[].width        | Integer  | 水印的宽度                                                                                                                              |
   | waterMark[].height       | Integer  | 水印的高度                                                                                                                              |
   | waterMark[].zorder       | Integer  | 水印的层级                                                                                                                              |
   | waterMark[].imgUrl       | String   | 图片水印的 URL，仅在 type 为 `picture` 时生效                                                                                           |
   | encryption               | Object   | 媒体流加密设置                                                                                                                          |
   | encryption.mode          | String   | 加密类型，支持 `AES_128_XTS`，`AES_128_ECB`，`AES_256_XTS`，`SM4_128_ECB`，`AES_128_GCM`，`AES_256_GCM`，`AES_128_GCM2`，`AES_256_GCM2` |
   | encryption.key           | String   | 加密密钥                                                                                                                                |
   | encryption.salt          | String   | 加密盐，值为 32 位字符，例如串 "ABC123"                                                                                                 |
   | rotation                 | Object[] | 画面旋转设置                                                                                                                            |
   | rotation[].uid           | String   | 需要旋转画面的用户 ID                                                                                                                   |
   | rotation[].degree        | Integer  | 旋转的角度，支持 0，90，180，270                                                                                                        |

   > **必读注意事项**：
   >
   > - 执行录制前务必正确填写 JSON 中的 `appId` 和 `token` 参数
   > - `appId` 和 `channelName` 的设置必须与声网 RTC SDK 中设置的完全一致
   > - 单流录制模式下，`recorderPath` 指定的是文件夹路径，必须手动确保该目录存在，例如设置 `"recorderPath": "recorder_result/"`，则需确保 `Examples-Cmd/recorder_result/` 目录已创建
   > - 确保 JSON 格式正确，不要漏掉逗号或引号等符号

#### 执行录制

1. 为单流录制创建输出目录：

   ```sh
   mkdir -p Examples-Cmd/recorder_result
   ```

2. 选择并运行对应的测试脚本：

   ```sh
   cd Examples-Cmd
   ./script/TestCaseName.sh
   ```

   可以根据需要修改脚本或对应的 JSON 配置文件，定制录制行为。

#### 常用测试脚本

`Examples-Cmd/script` 目录下提供了多种预设的测试脚本：

| 脚本名称                                         | 功能描述                                   |
| ------------------------------------------------ | ------------------------------------------ |
| MixStreamRecordingAudioVideo.sh                  | 混流录制音视频                             |
| MixStreamRecordingAudio.sh                       | 混流仅录制音频                             |
| MixStreamRecordingVideo.sh                       | 混流仅录制视频                             |
| MixStreamRecordingAudioVideoWatermarks.sh        | 混流录制音视频并添加水印                   |
| MixStreamRecordingAudioVideoWatermarksBg.sh      | 混流录制音视频，添加水印和背景             |
| MixStreamRecordingAudioVideoWatermarksRecover.sh | 混流录制音视频，添加水印并启用录制恢复功能 |
| MixStreamRecordingAudioVideoEncryption.sh        | 混流录制音视频并启用加密                   |
| MixStreamRecordingAudioVideoStringUid.sh         | 使用字符串用户 ID 的混流录制音视频         |
| SingleStreamRecordingAudioVideo.sh               | 单流录制音视频                             |
| SingleStreamRecordingAudio.sh                    | 单流仅录制音频                             |
| SingleStreamRecordingVideo.sh                    | 单流仅录制视频                             |
| SingleStreamRecordingAudioVideoWatermarks.sh     | 单流录制音视频并添加水印                   |

选择合适的脚本，或基于现有脚本创建自定义的录制配置。每个脚本都对应 `config` 目录下的同名配置文件。

#### 控制录制过程

- **开始录制**：脚本执行后自动开始录制
- **结束录制**：在命令行中输入 `1` 并按回车，程序将停止录制并退出

#### 录制输出文件

- **单流录制**：在 `Examples-Cmd/recorder_result/` 目录下生成多个 MP4 文件，以各用户的 UID 命名
- **混合录制**：在 `Examples-Cmd` 目录下生成单个 MP4 文件，文件名按 JSON 配置指定

#### 常见问题排查

- 如果录制没有输出文件，检查 AppID、Token 和频道名是否正确
- 确保频道中有活跃用户在发送媒体流
- 检查日志文件了解详细错误信息，日志通常位于 `Examples-Cmd/logs/` 目录

> **提示**：更多高级配置选项和详细参数说明，请参考 `Examples-Cmd/config/recorder_json.example` 文件中的注释。

### 调用 API 录制

#### 前提条件

开始前请确保你已经完成录制 SDK 的环境准备和集成工作，包括配置 jar 和对应平台的 so。

#### 调用 API 实现录制

以下示例代码基于 `Examples-Cmd` 目录中的实际示例项目，展示了如何使用录制 SDK API 进行录制。

##### 初始化服务

```java
// 创建 AgoraService 实例
AgoraService agoraService = new AgoraService();

// 配置本地代理，配置必须放在 initialize 之前
LocalAccessPointConfiguration localAccessPointConfig = new LocalAccessPointConfiguration();
localAccessPointConfig.setMode(Constants.LocalProxyMode.LocalOnly);
localAccessPointConfig.setDomainList(new String[] { "" });
localAccessPointConfig.setIpList(new String[] { "10.xx.xx.xx" });
localAccessPointConfig.setDomainListSize(1);
localAccessPointConfig.setIpListSize(1);
localAccessPointConfig.setVerifyDomainName("ap.xxx.agora.local");
int setGlobalLocalAccessPointRet = agoraService.setGlobalLocalAccessPoint(localAccessPointConfig);

// 创建并配置服务配置对象
AgoraServiceConfiguration config = new AgoraServiceConfiguration();
config.setEnableAudioDevice(false);    // 是否启用音频设备（通常设为 false）
config.setEnableAudioProcessor(true);  // 启用音频处理
config.setEnableVideo(true);           // 启用视频功能
config.setAppId("您的APPID");           // 设置您的 App ID
config.setUseStringUid(false);         // 是否使用字符串 UID
agoraService.initialize(config);       // 初始化服务

// 可选：设置云代理
AgoraParameter parameter = agoraService.getAgoraParameter();
if (parameter != null) {
    parameter.setBool("rtc.enable_proxy", true);
}
```

##### 加入频道

```java
// 创建媒体组件工厂
AgoraMediaComponentFactory factory = agoraService.createAgoraMediaComponentFactory();

// 创建并初始化录制器
AgoraMediaRtcRecorder agoraMediaRtcRecorder = factory.createMediaRtcRecorder();
// 第二个参数表示是否启用混流录制：true=混流，false=单流
agoraMediaRtcRecorder.initialize(agoraService, false);

// 创建并注册事件处理器
IAgoraMediaRtcRecorderEventHandler handler = new AgoraMediaRtcRecorderEventHandler();
agoraMediaRtcRecorder.registerRecorderEventHandler(handler);

// 加入频道
agoraMediaRtcRecorder.joinChannel(
    "您的Token",        // 频道 Token，如不启用 Token 验证可为 null
    "您的频道名",       // 频道名称
    "0"                // 用户 ID，如果设置为 0 将由系统自动分配
);
```

##### 配置和开始录制

```java
// 订阅音频流
if (需要订阅所有音频) {
    agoraMediaRtcRecorder.subscribeAllAudio();
} else {
    // 仅订阅特定用户的音频
    agoraMediaRtcRecorder.subscribeAudio("用户ID");
}

// 订阅视频流
VideoSubscriptionOptions options = new VideoSubscriptionOptions();
options.setEncodedFrameOnly(false);
options.setType(VideoStreamType.VIDEO_STREAM_HIGH); // 可选：VIDEO_STREAM_LOW
if (需要订阅所有视频) {
    agoraMediaRtcRecorder.subscribeAllVideo(options);
} else {
    // 仅订阅特定用户的视频
    agoraMediaRtcRecorder.subscribeVideo("用户ID", options);
}

// 配置混流布局（仅在混流模式下需要）
if (启用混流) {
    VideoMixingLayout layout = new VideoMixingLayout();
    // 配置布局参数...
    agoraMediaRtcRecorder.setVideoMixingLayout(layout);
}

// 配置录制参数
MediaRecorderConfiguration mediaRecorderConfiguration = new MediaRecorderConfiguration();
mediaRecorderConfiguration.setWidth(640);       // 设置录制视频宽度
mediaRecorderConfiguration.setHeight(480);      // 设置录制视频高度
mediaRecorderConfiguration.setFps(15);          // 设置录制帧率
mediaRecorderConfiguration.setMaxDurationMs(60 * 60 * 1000); // 最大录制时长，单位毫秒
mediaRecorderConfiguration.setStoragePath("/path/to/save/recording.mp4"); // 录制文件保存路径

// 合流录制配置
agoraMediaRtcRecorder.setRecorderConfig(mediaRecorderConfiguration);

// 或者单流录制配置
//agoraMediaRtcRecorder.setRecorderConfigByUid(mediaRecorderConfiguration, "用户ID");

// 添加水印（可选）
WatermarkConfig[] watermarks = new WatermarkConfig[1];
watermarks[0] = new WatermarkConfig();
// 配置水印参数...
agoraMediaRtcRecorder.enableAndUpdateVideoWatermarks(watermarks);

// 启用加密（可选）
if (需要加密) {
    EncryptionConfig encryptionConfig = new EncryptionConfig();
    encryptionConfig.setEncryptionMode(EncryptionMode.AES_128_GCM); // 设置加密模式
    encryptionConfig.setEncryptionKey("加密密钥");
    agoraMediaRtcRecorder.enableEncryption(true, encryptionConfig);
}

// 开始录制
if (启用混流) {
    agoraMediaRtcRecorder.startRecording();
} else {
    // 单流录制
    agoraMediaRtcRecorder.startSingleRecordingByUid("用户ID");
}
```

##### 录制事件处理

```java
public static class AgoraMediaRtcRecorderEventHandler implements IAgoraMediaRtcRecorderEventHandler {
    @Override
    public void onFirstRemoteAudioDecoded(String channelId, String userId, int elapsed) {
        // 首次检测到远程音频解码时触发，可在此开始单流音频录制
        new Thread() {
            @Override
            public void run() {
                MediaRecorderConfiguration mediaRecorderConfiguration = new MediaRecorderConfiguration();
                // 配置录制参数...
                agoraMediaRtcRecorder.setRecorderConfigByUid(mediaRecorderConfiguration, userId);
                agoraMediaRtcRecorder.startSingleRecordingByUid(userId);
            }
        }.start();
    }

    @Override
    public void onFirstRemoteVideoDecoded(String channelId, String userId, int width, int height, int elapsed) {
        // 首次检测到远程视频解码时触发，可在此更新混流布局或开始单流视频录制
        new Thread() {
            @Override
            public void run() {
                if (启用混流) {
                    VideoMixingLayout layout = new VideoMixingLayout();
                    // 配置混流布局...
                    agoraMediaRtcRecorder.setVideoMixingLayout(layout);
                } else {
                    MediaRecorderConfiguration mediaRecorderConfiguration = new MediaRecorderConfiguration();
                    // 配置录制参数...
                    agoraMediaRtcRecorder.setRecorderConfigByUid(mediaRecorderConfiguration, userId);
                    agoraMediaRtcRecorder.startSingleRecordingByUid(userId);
                }
            }
        }.start();
    }

    @Override
    public void onRecorderStateChanged(String channelId, String userId, Constants.RecorderState state,
            Constants.RecorderReasonCode reason, String fileName) {
        // 录制状态变化回调，可据此了解录制进程
    }

    // 其他事件处理方法...
}
```

##### 结束录制

```java
// 取消订阅流
agoraMediaRtcRecorder.unsubscribeAllAudio();
agoraMediaRtcRecorder.unsubscribeAllVideo();

// 停止录制
if (启用混流) {
    agoraMediaRtcRecorder.stopRecording();
} else {
    // 停止单流录制
    agoraMediaRtcRecorder.stopSingleRecordingByUid("用户ID");
}

// 注销事件处理器
agoraMediaRtcRecorder.unregisterRecorderEventHandle(handler);

// 离开频道并释放资源
agoraMediaRtcRecorder.leaveChannel();
agoraMediaRtcRecorder.release();

// 释放服务
agoraService.release();
```

##### 获取录制文件

录制文件将根据录制类型保存在不同位置：

- **单流录制**：在 `Examples-Cmd` 目录下指定文件夹下生成单流录制的 mp4 文件，文件名是 UID 开头的，如 `uid_123456_timestamp.mp4`。

- **合流录制**：在 `Examples-Cmd` 目录下生成合流的录制 mp4 文件，文件名是通过 `MediaRecorderConfiguration` 对象的 `storagePath` 参数配置的。

在实际应用中，建议为每次录制设置唯一的文件路径，可以使用频道名、时间戳等作为文件名的一部分，以避免文件覆盖。

更多录制选项和高级功能，请参考 `MediaRecorderConfiguration` 类的 API 文档。

### 跑通 Maven 工程

本 SDK 提供了基于 Spring Boot 的 Maven 示例工程，方便你快速验证和二次开发。以下为跑通 `Examples-Mvn` 工程的基本流程：

#### 1. 编译打包

进入 `Examples-Mvn` 目录，执行：

```sh
mvn clean package
```

编译成功后，会在 `target/` 目录下生成 `agora-example.jar`。

#### 2. 配置密钥

在 `Examples-Mvn` 目录下创建 `.keys` 文件，内容如下（请替换为你的实际信息）：

```
appId=你的AppId
token=你的Token
```

#### 3. 准备 so 库

确保 `libs/native/linux/x86_64/` 目录下包含所有必要的 so 文件（如 `libagora_rtc_sdk.so`、`librecording.so` 等）。

#### 4. 运行示例服务

在 `Examples-Mvn` 目录下执行：

```sh
LD_LIBRARY_PATH="$LD_LIBRARY_PATH:libs/native/linux/x86_64" java -Dserver.port=18080 -jar target/agora-example.jar
```

- 该命令会启动 Spring Boot 服务，监听 18080 端口。
- 如需更换端口，可修改 `-Dserver.port` 参数。

#### 5. 通过 API 启动/停止录制

- 启动录制：
  ```
  http://<服务器IP>:18080/api/recording/start?configFileName=mix_stream_recorder_audio_video_water_marks.json
  ```
- 停止录制：
  ```
  http://<服务器IP>:18080/api/recording/stop?taskId=<任务ID>
  ```

> 录制配置文件需放在 `Examples-Mvn/src/main/resources/` 目录下。

#### 6. 常见问题

- 若服务无法启动，请检查 so 文件路径、.keys 文件内容及端口占用。
- 录制无输出时，请检查频道内有无活跃用户、AppId/Token/频道名是否正确。

---

## API 参考

有关 SDK API 的详细说明，请参考 [API-reference.zh.md](API-reference.zh.md) 文档，每个类和方法都提供了详细的参数说明、返回值解释。

## 更新日志

### v4.4.150.4（2025-06-11）

#### API 变更

- **新增**：`AgoraMediaRtcRecorder` 类新增 `renewToken` 方法，支持动态更新频道 Token，避免 Token 过期导致录制中断
- **修改**：将 `Constants.WaterMaskFitMode` 重命名为 `Constants.WatermarkFitMode`，修正拼写错误并保持命名一致性

#### 改进与优化

- **修复**：修复 `MixerLayoutConfig` 类中 `imagePath` 属性设置问题，确保背景图片路径正确配置

### v4.4.150.3（2025-05-20）

#### API 变更

- **新增**：`IAgoraMediaRtcRecorderEventHandler` 新增 `onError`、`onTokenPrivilegeWillExpire`、`onTokenPrivilegeDidExpire` 回调方法，支持错误上报及 Token 即将过期/已过期通知。

### v4.4.150.2（2025-05-09）

#### API 变更

- **新增**：`AgoraService` 类中添加 `setGlobalLocalAccessPoint` 函数，用于配置全局本地接入点。

#### 改进与优化

- **修复**：修复 SpringBot 打包回调处理问题

### v4.4.150.1（2025-03-28）

#### API 变更

- **新增**：`IAgoraMediaRtcRecorderEventHandler` 类中添加 `onEncryptionError` 回调函数，支持加密错误通知
- **新增**：`AgoraMediaRtcRecorder` 类中添加 `setAudioVolumeIndicationParameters` 方法，用于配置远端用户音量回调间隔
- **重构**：优化 `IAgoraMediaRtcRecorderEventHandler` 类中 `onAudioVolumeIndication` 回调函数的参数结构

#### 改进与优化

- **增强**：改进 `VideoMixingLayout` 类，修复 `backgroundColor` 属性
- **新功能**：`VideoMixingLayout` 类新增 `backgroundImage` 属性，支持设置背景图片

### v4.4.150-aarch64（2025-02-24）

#### API 变更

- **兼容**：与 v4.4.150 版本保持 API 兼容

#### 改进与优化

- **平台**：首次支持 ARM64 架构

### v4.4.150（2025-01-21）

#### API 变更

- **初始版本**：发布基础 API 结构

#### 改进与优化

- **性能**：基础功能和性能优化

## 其他参考

详细参考官网（<https://doc.shengwang.cn/doc/recording/java/landing-page>）
