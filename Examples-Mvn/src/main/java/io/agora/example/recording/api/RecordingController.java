package io.agora.example.recording.api;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.agora.example.recording.agora.AgoraServiceInitializer;
import io.agora.example.recording.agora.RecorderConfig;
import io.agora.example.recording.agora.RecordingManager;
import io.agora.example.recording.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;

@Slf4j
@RestController
@RequestMapping("/api/recording")
public class RecordingController implements DisposableBean, ApplicationContextAware {

    private final RecordingManager recordingManager;
    private final AgoraServiceInitializer agoraServiceInitializer;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Gson gson;
    private final Map<String, String> activeTaskIds = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public RecordingController() {
        this.recordingManager = new RecordingManager();
        this.agoraServiceInitializer = new AgoraServiceInitializer();
        this.gson = new Gson();
        log.info("RecordingController initialized successfully.");
    }

    private String readFileFromResources(String fileName) {
        log.debug("Attempting to read file from resources: {}", fileName);
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(fileName);

        if (resourceStream == null) {
            log.error("Cannot find file in classpath: {}", fileName);
            throw new RuntimeException("Cannot find file in classpath: " + fileName);
        }

        try (InputStream inputStream = resourceStream;
                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            StringBuilder stringBuilder = new StringBuilder();
            int numRead;
            while ((numRead = reader.read(buffer, 0, buffer.length)) != -1) {
                stringBuilder.append(buffer, 0, numRead);
            }
            log.debug("Successfully read file from resources: {}, content length: {}", fileName,
                    stringBuilder.length());
            return stringBuilder.toString();
        } catch (Exception e) {
            log.error("Error reading file from resources: {}", fileName, e);
            throw new RuntimeException("Error reading file: " + fileName, e);
        }
    }

    /**
     * 获取 resources 目录下所有以 mix_stream 或 single_stream 开头的配置文件
     */
    private List<String> getConfigFiles() {
        List<String> configFiles = new ArrayList<>();
        String resourceFolderPath = ""; // Root of resources, where JSON files are expected

        try {
            URL resourceUrl = getClass().getClassLoader().getResource(resourceFolderPath);
            if (resourceUrl == null) {
                log.warn("Cannot find resource folder path: '{}' in classpath.", resourceFolderPath);
                return configFiles;
            }

            URI resourceUri = resourceUrl.toURI();
            Path path; // Path to the directory to scan

            FileSystem jarFileSystemToManage = null; // To hold a FileSystem we might create and need to close

            try {
                if ("jar".equals(resourceUri.getScheme())) {
                    String[] parts = resourceUri.toString().split("!");
                    if (parts.length < 2) {
                        log.error("Invalid JAR URI, missing '!' separator: {}", resourceUri);
                        return configFiles;
                    }
                    URI jarFileUri = new URI(parts[0]); // URI of the JAR file itself, e.g., jar:file:/path/to/app.jar
                    String internalPath = parts[1]; // Path inside the JAR, e.g., /BOOT-INF/classes/ or /

                    try {
                        // Attempt to get an existing file system for the JAR file URI
                        jarFileSystemToManage = FileSystems.getFileSystem(jarFileUri);
                    } catch (FileSystemNotFoundException ex) {
                        // If not found, create a new one. This instance we "own" and would ideally
                        // close.
                        // For this scope, we'll create it and rely on its lifecycle or JVM shutdown.
                        // A robust solution might use a shared FS manager or try-with-resources if
                        // scope allows.
                        jarFileSystemToManage = FileSystems.newFileSystem(jarFileUri, Collections.emptyMap());
                    }
                    path = jarFileSystemToManage.getPath(internalPath);

                } else {
                    // Regular file system path
                    path = Paths.get(resourceUri);
                }

                if (Files.isDirectory(path)) {
                    try (Stream<Path> walk = Files.walk(path, 1)) { // Depth 1: only files directly in this directory
                        configFiles = walk
                                .filter(Files::isRegularFile)
                                .map(p -> p.getFileName().toString())
                                .filter(fileName -> (fileName.startsWith("mix_stream_")
                                        || fileName.startsWith("single_stream_")) && fileName.endsWith(".json"))
                                .collect(Collectors.toList());
                        log.info("Scanned directory '{}'. Found {} potential config files: {}", path,
                                configFiles.size(), configFiles);
                    } catch (IOException e) {
                        log.error("Error walking directory path: {}", path, e);
                    }
                } else {
                    log.warn("Resource path '{}' resolved to '{}' is not a directory. Cannot scan for files.",
                            resourceUrl, path);
                }
            } catch (FileSystemAlreadyExistsException e) {
                // This can occur if FileSystems.newFileSystem is called when one already
                // exists.
                // Our logic now tries getFileSystem first, so this should be less common here.
                // If it still happens, it points to complex FS management issues.
                log.error(
                        "FileSystemAlreadyExistsException for JAR URI related to {}. This may indicate concurrent FS creation attempts or mismanagement.",
                        resourceUri, e);
                // Attempt to proceed if path could be resolved, otherwise list will be empty.
            }
            // Note: Closing jarFileSystemToManage is complex due to shared nature if
            // obtained via getFileSystem.
            // If created via newFileSystem, it should ideally be closed.
            // For simplicity in this context, explicit close is omitted, relying on broader
            // lifecycle management.

        } catch (URISyntaxException e) {
            log.error("Invalid URI syntax for resource path: '{}'", resourceFolderPath, e);
        } catch (IOException e) { // Catch IOException from FileSystem operations
            log.error("IO error processing resource path: '{}'", resourceFolderPath, e);
        } catch (Exception e) { // Catch any other unexpected exceptions
            log.error("An unexpected error occurred while trying to list config files from path: '{}'",
                    resourceFolderPath, e);
        }

        // Verification loop: ensure found files are actually loadable as resources
        List<String> verifiedConfigFiles = new ArrayList<>();
        if (!configFiles.isEmpty()) {
            log.debug("Verifying {} found file names by attempting to load as resources...", configFiles.size());
            for (String fileName : configFiles) {
                // getResourceAsStream takes path relative to classpath root.
                // Since resourceFolderPath is "", fileName is already the correct path.
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
                    if (is != null) {
                        verifiedConfigFiles.add(fileName);
                        log.debug("Successfully verified and added config file: {}", fileName);
                    } else {
                        // This can happen if the file exists by name in the directory listing
                        // but is not actually a loadable resource (e.g., due to JAR packaging issues or
                        // incorrect path).
                        log.warn(
                                "File '{}' found by scan but could not be loaded as a resource stream from classpath root.",
                                fileName);
                    }
                } catch (IOException e) {
                    log.warn("IOException when trying to verify resource stream for file '{}': {}", fileName,
                            e.getMessage());
                }
            }
        }

        if (verifiedConfigFiles.isEmpty() && !configFiles.isEmpty()) {
            log.warn(
                    "Found {} files by name matching pattern, but none could be verified as loadable resources from classpath.",
                    configFiles.size());
        } else if (verifiedConfigFiles.isEmpty()) {
            log.warn(
                    "No config files found matching criteria or verifiable after scanning. Base path for scan: '{}' (classpath root).",
                    resourceFolderPath);
        }

        log.info("Returning {} verified config files: {}", verifiedConfigFiles.size(), verifiedConfigFiles);
        return verifiedConfigFiles;
    }

    private String formatMapToString(Map<String, Object> map, String currentIndent) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(currentIndent).append(entry.getKey()).append(": ");
            Object value = entry.getValue();
            if (value instanceof Map) {
                // noinspection unchecked
                sb.append("\n").append(formatMapToString((Map<String, Object>) value, currentIndent + "  "));
            } else if (value instanceof List) {
                sb.append("\n");
                // noinspection unchecked
                List<Object> listValue = (List<Object>) value;
                for (Object item : listValue) {
                    if (item instanceof Map) {
                        // noinspection unchecked
                        sb.append(currentIndent).append("  - Map:\n");
                        sb.append(formatMapToString((Map<String, Object>) item, currentIndent + "    "));
                    } else {
                        sb.append(currentIndent).append("  - ").append(item != null ? item.toString() : "null")
                                .append("\n");
                    }
                }
            } else {
                sb.append(value != null ? value.toString() : "null").append("\n");
            }
        }
        return sb.toString();
    }

    // Public entry point for the formatter
    private String formatMapToString(Map<String, Object> map) {
        // Wrap the string representation in <pre> tags for browser rendering
        return "<pre>" + formatMapToString(map, "") + "</pre>";
    }

    @GetMapping(value = "/start")
    public SseEmitter startRecording(@RequestParam(required = false) String configFileName) {
        log.info("=== START SSE RECORDING REQUEST ===");
        log.info("Received configFileName parameter: {}", configFileName);
        log.info("Current active tasks count: {}", activeTaskIds.size());

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Use a long timeout or configure appropriately

        // Use the existing executorService to run the SSE task
        executorService.execute(() -> {
            try {

                sendSseEvent(emitter, "log",
                        String.format("Operation: start, ConfigFileName: %s, BeforeActiveCount: %d",
                                configFileName == null ? "ALL" : configFileName, activeTaskIds.size()));

                if (configFileName == null || configFileName.trim().isEmpty()) {
                    log.info("ConfigFileName is empty, will start recording for all config files via SSE");
                    startMultipleRecordingsSse(emitter);
                } else {
                    log.info("ConfigFileName provided: {}, starting single recording via SSE", configFileName);
                    startSingleRecordingSse(configFileName.trim(), emitter);
                }
                // Send a final completion message
                sendSseEvent(emitter, "completed",
                        "All recording tasks processing finished. Active tasks: " + activeTaskIds.size());
                emitter.complete();
            } catch (Exception e) {
                log.error("Error in SSE task for startRecording", e);
                try {
                    // This call to sendSseEvent can also throw IOException
                    sendSseEvent(emitter, "log", "Error in SSE task: " + e.getMessage());
                } catch (Exception ex) { // Catch any exception from sending the error message itself
                    log.warn("Failed to send SSE error message to client: {}", ex.getMessage());
                }
                emitter.completeWithError(e);
            }
        });

        log.info("SseEmitter returned to client for /start endpoint.");
        return emitter;
    }

    private void startSingleRecordingSse(String configFileName, SseEmitter emitter) {
        String taskId = Utils.getTaskId();
        sendSseEvent(emitter, "log",
                String.format("--- Starting single recording --- TaskId: %s, ConfigFile: %s", taskId, configFileName));

        try {
            String jsonConfigContent = readFileFromResources(configFileName);

            RecorderConfig config = gson.fromJson(jsonConfigContent, RecorderConfig.class);

            String[] keys = Utils.readAppIdAndToken(".keys");
            if (keys != null && keys.length == 2 && !io.agora.recording.utils.Utils.isNullOrEmpty(keys[0])
                    && !io.agora.recording.utils.Utils.isNullOrEmpty(keys[1])) {
                config.setAppId(keys[0]);
                config.setToken(keys[1]);

            } else {
                sendSseEvent(emitter, "log", String.format(
                        "WARN: Could not load AppId/Token from .keys for taskId: %s. Using config values.", taskId));
                log.warn(
                        "Could not load AppId and Token from .keys file for taskId: {}. Ensure .keys file is present or AppId/Token are in JSON.",
                        taskId);
            }

            sendSseEvent(emitter, "log",
                    String.format("Final recording config for taskId %s: %s", taskId, config.toString())); // Consider a
                                                                                                           // shorter
                                                                                                           // summary

            agoraServiceInitializer.initService(config);

            // The actual recording start is already async. We just need to capture its
            // initiation.
            // We can't easily get a direct success/failure from the submit() itself for SSE
            // here
            // unless recordingManager.startRecording was refactored to be blocking or
            // return a Future
            // and we wait on it. For now, we'll signal initiation.
            // The original activeTaskIds.put happens *inside* the async task.
            // For SSE, it's better if activeTaskIds.put happens before signalling success
            // via SSE if possible,
            // or the message clearly states "initiation started".

            final String originalTaskId = taskId; // effectively final for lambda
            executorService.submit(() -> {
                try {
                    log.info("Starting async recording execution for taskId: {} (from SSE path)", originalTaskId);
                    recordingManager.startRecording(originalTaskId, config);
                    activeTaskIds.put(originalTaskId, configFileName); // Now it's confirmed and active
                    log.info("Recording successfully started for taskId: {} with config file: {}. Active tasks: {}",
                            originalTaskId, configFileName, activeTaskIds.size());
                    // We cannot reliably send SSE event from here as the emitter might be closed if
                    // the main SSE thread finished.
                    // The success is implied by not seeing an error from the *initiation* step.
                    // Client will see "Successfully initiated..." and then if an error specific to
                    // this task happens async, it's harder to report back on THIS SseEmitter.
                    // This is a limitation if startRecording itself is a fire-and-forget within an
                    // executor.
                } catch (Exception e) {
                    log.error("Error during async recording for taskId: {} (from SSE path)", originalTaskId, e);
                    // This error is hard to propagate back to the specific SseEmitter for *this*
                    // user's request.
                }
            });

            // activeTaskIds.put(taskId, configFileName); // This should happen upon actual
            // start, which is async.
            // For now, let's assume initiation implies it will soon be active.
            // This makes the "afterActiveCount" a bit speculative.

        } catch (Exception e) {
            log.error("Failed to start recording for taskId: {} with config file: {} (SSE path)", taskId,
                    configFileName, e);
            sendSseEvent(emitter, "log",
                    String.format("ERROR: Failed to start recording for taskId: %s, config: %s. Error: %s", taskId,
                            configFileName, e.getMessage()));
        }
    }

    private void startMultipleRecordingsSse(SseEmitter emitter) {
        sendSseEvent(emitter, "log", "--- Starting multiple recordings via SSE ---");
        List<String> configFiles = getConfigFiles();

        if (configFiles.isEmpty()) {
            sendSseEvent(emitter, "log", "WARN: No config files found in resources directory.");
            log.warn("No config files found in resources directory (SSE path)");
            return;
        }

        sendSseEvent(emitter, "log", String
                .format("Found %d config files. Will start recordings with 1 second interval.", configFiles.size()));
        log.info("Will start {} recordings with 1 second interval (SSE path)", configFiles.size());

        int currentStarted = 0;
        int currentFailed = 0;

        for (int i = 0; i < configFiles.size(); i++) {
            String configFile = configFiles.get(i);
            sendSseEvent(emitter, "log",
                    String.format("Processing config file %d/%d: %s", i + 1, configFiles.size(), configFile));

            // Re-using startSingleRecordingSse means it will generate its own taskId and
            // send its own detailed SSE messages.
            // This is good for consistency.
            // We capture if the *initiation* of this single recording had an issue from
            // startSingleRecordingSse's perspective.
            // However, startSingleRecordingSse doesn't return success/failure directly.
            // We'll assume if no exception is thrown *during its synchronous part*, it's
            // "initiated".

            // For a more robust success/failure count here, startSingleRecordingSse would
            // need to return a status
            // or we'd need to change its internal error handling.
            // For now, we just call it. Its own SSE messages will inform the client.
            try {
                startSingleRecordingSse(configFile, emitter); // This will send its own detailed SSE logs
                currentStarted++; // Assuming initiation was successful if no immediate exception.
            } catch (Exception e) { // This catch might not be hit if startSingle errors are handled inside & sent
                                    // via SSE
                log.error("Outer catch: Exception while trying to initiate single recording for {} via SSE: {}",
                        configFile, e.getMessage());
                sendSseEvent(emitter, "log", String.format("ERROR: Outer exception during initiation for %s: %s",
                        configFile, e.getMessage()));
                currentFailed++;
            }

            if (i < configFiles.size() - 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.warn("Thread interrupted during 1s delay in startMultipleRecordingsSse", e);
                    sendSseEvent(emitter, "log", "WARN: Interrupted while waiting for next recording.");
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    break; // Exit loop if interrupted
                }
            }
        }
        // This summary is a bit tricky as success of individual tasks is async.
        // The counts here reflect successful *initiations*.
        sendSseEvent(emitter, "log", String.format(
                "Multiple recordings processing attempt finished. Initiations: %d attempted (this count might differ from actual success due to async nature).",
                configFiles.size()));
    }

    // Helper method to send SSE events
    private void sendSseEvent(SseEmitter emitter, String eventName, String data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            // Log error or handle client disconnects
            log.warn("Failed to send SSE event: {} - {}, error: {}", eventName, data, e.getMessage());
            // Consider if we should throw a runtime exception to stop the SSE stream
            // For now, just log and continue if possible.
        }
    }

    @GetMapping(value = "/stop")
    public SseEmitter stopRecording(@RequestParam(name = "taskId", required = false) String taskId) {
        log.info("=== SSE STOP RECORDING REQUEST ===");
        log.info("Received taskId parameter: {}", taskId);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executorService.execute(() -> {
            try {
                sendSseEvent(emitter, "log", String.format("Operation: stop, TaskId: %s, BeforeActiveCount: %d",
                        taskId == null ? "ALL" : taskId, activeTaskIds.size()));

                if (!io.agora.recording.utils.Utils.isNullOrEmpty(taskId)) {
                    stopSingleRecordingSse(taskId.trim(), emitter);
                } else {
                    stopAllRecordingsSse(emitter);
                }
                sendSseEvent(emitter, "completed",
                        "Stop operation processing finished. Active tasks: " + activeTaskIds.size());
                emitter.complete();
            } catch (Exception e) {
                log.error("Error in SSE task for stopRecording", e);
                try {
                    sendSseEvent(emitter, "log", "Error in SSE task for stop: " + e.getMessage());
                } catch (Exception ex) {
                    log.warn("Failed to send SSE error message to client for stop: {}", ex.getMessage());
                }
                emitter.completeWithError(e);
            }
        });
        log.info("SseEmitter returned to client for /stop endpoint.");
        return emitter;
    }

    private void stopSingleRecordingSse(String taskId, SseEmitter emitter) {
        sendSseEvent(emitter, "log", String.format("--- Stopping single recording (SSE) --- TaskId: %s", taskId));

        try {
            String configFileName = activeTaskIds.get(taskId);
            if (configFileName == null) {
                log.warn("TaskId {} not found in active tasks (SSE)", taskId);
                sendSseEvent(emitter, "log", String.format("WARN: TaskId %s not found in active recordings.", taskId));
                sendSseEvent(emitter, "status", String.format(
                        "Single stop failed: TaskId %s not found. AfterActiveCount: %d", taskId, activeTaskIds.size()));
                return;
            }

            sendSseEvent(emitter, "log",
                    String.format("Found TaskId %s with ConfigFile: %s. Attempting to stop.", taskId, configFileName));
            recordingManager.stopRecording(taskId, false);
            activeTaskIds.remove(taskId);

            log.info("Recording stopped successfully for taskId: {} (SSE)", taskId);
            sendSseEvent(emitter, "log", String.format("SUCCESS: Recording stopped for TaskId: %s. ConfigFile: %s.",
                    taskId, configFileName));
            sendSseEvent(emitter, "status", String.format("Single stop success: TaskId %s. AfterActiveCount: %d",
                    taskId, activeTaskIds.size()));

        } catch (Exception e) {
            log.error("Failed to stop recording for taskId: {} (SSE)", taskId, e);
            sendSseEvent(emitter, "log",
                    String.format("ERROR: Failed to stop recording for TaskId: %s. Error: %s", taskId, e.getMessage()));
            sendSseEvent(emitter, "status",
                    String.format("Single stop error: TaskId %s. AfterActiveCount: %d", taskId, activeTaskIds.size()));
        }
    }

    private void stopAllRecordingsSse(SseEmitter emitter) {
        sendSseEvent(emitter, "log", "--- Stopping all recordings (SSE) ---");
        Map<String, String> tasksToStopSnapshot = new ConcurrentHashMap<>(activeTaskIds);
        sendSseEvent(emitter, "log", String.format("Will attempt to stop %d active recordings: %s",
                tasksToStopSnapshot.size(), tasksToStopSnapshot.keySet()));

        int successfullyStoppedCount = 0;
        int failedToStopCount = 0;

        for (Map.Entry<String, String> entry : tasksToStopSnapshot.entrySet()) {
            String idToStop = entry.getKey();
            String configFile = entry.getValue();
            sendSseEvent(emitter, "log",
                    String.format("Attempting to stop TaskId: %s (Config: %s)", idToStop, configFile));

            try {
                recordingManager.stopRecording(idToStop, false);
                activeTaskIds.remove(idToStop);
                sendSseEvent(emitter, "log",
                        String.format("Successfully stopped TaskId: %s (Config: %s)", idToStop, configFile));
                successfullyStoppedCount++;
            } catch (Exception e) {
                log.error(
                        "Failed to stop recording for taskId: {} (config: {}). It will be removed from active list. (SSE)",
                        idToStop, configFile, e);
                activeTaskIds.remove(idToStop); // Ensure removal even on failure to stop
                sendSseEvent(emitter, "log",
                        String.format("ERROR stopping TaskId: %s (Config: %s): %s. Removed from active list.", idToStop,
                                configFile, e.getMessage()));
                failedToStopCount++;
            }
        }
        sendSseEvent(emitter, "log", String.format(
                "Finished attempting to stop %d targeted active recordings. %d stopped successfully, %d failed.",
                tasksToStopSnapshot.size(), successfullyStoppedCount, failedToStopCount));
        sendSseEvent(emitter, "status",
                String.format("Stop all complete. SuccessfullyStopped: %d, FailedToStop: %d, AfterActiveCount: %d",
                        successfullyStoppedCount, failedToStopCount, activeTaskIds.size()));
    }

    @GetMapping(value = "/status", produces = "text/html;charset=UTF-8")
    public String getRecordingStatus() {
        log.info("=== GET RECORDING STATUS REQUEST ===");
        log.info("Current active tasks count: {}", activeTaskIds.size());
        log.info("Current active tasks: {}", activeTaskIds);

        Map<String, Object> status = new HashMap<>();
        status.put("activeTasksCount", activeTaskIds.size());
        status.put("activeTasks", new HashMap<>(activeTaskIds));
        status.put("timestamp", System.currentTimeMillis());

        log.info("Returning status: {}", status);
        return formatMapToString(status);
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/recording.html";
    }

    @GetMapping(value = "/destroy")
    public SseEmitter destroyApplication() {
        log.info("=== SSE DESTROY APPLICATION REQUEST RECEIVED ===");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executorService.execute(() -> {
            try {
                sendSseEvent(emitter, "log",
                        String.format("Operation: destroyApplication, Timestamp: %d", System.currentTimeMillis()));

                if (activeTaskIds.size() > 0) {
                    sendSseEvent(emitter, "log",
                            "Active recordings found. Attempting to stop all before destroying service.");
                    stopAllRecordingsSse(emitter); // Reuse the SSE version
                } else {
                    sendSseEvent(emitter, "log", "No active recordings to stop.");
                }

                sendSseEvent(emitter, "log",
                        "Step 1: Releasing recording resources by calling internal destroy() method...");
                try {
                    this.destroy(); // Calls the DisposableBean destroy()
                    sendSseEvent(emitter, "log", "Recording resources released successfully.");
                } catch (Exception e) {
                    log.error("Error during resource cleanup phase of application destruction (SSE)", e);
                    sendSseEvent(emitter, "log", "ERROR during resource cleanup: " + e.getMessage());
                    // Continue with shutdown attempt
                }

                sendSseEvent(emitter, "log", "Step 2: Initiating Spring Boot application shutdown...");
                if (this.applicationContext != null) {
                    sendSseEvent(emitter, "log",
                            "Application shutdown process will be initiated shortly. This might be the last message.");
                    emitter.complete(); // Complete SSE stream before starting shutdown thread

                    new Thread(() -> {
                        try {
                            Thread.sleep(500); // Short delay
                            log.info("Executing asynchronous application shutdown (from SSE /destroy)...");
                            int exitCode = SpringApplication.exit(applicationContext, () -> 0);
                            log.info("SpringApplication.exit() called via SSE /destroy. Exiting with code: {}",
                                    exitCode);
                            System.exit(exitCode);
                        } catch (InterruptedException e) {
                            log.warn(
                                    "Shutdown thread interrupted while waiting to exit (SSE /destroy). Forcing System.exit(1).",
                                    e);
                            Thread.currentThread().interrupt();
                            System.exit(1);
                        } catch (Exception e) {
                            log.error("Error during SpringApplication.exit() (SSE /destroy). Forcing System.exit(1).",
                                    e);
                            System.exit(1);
                        }
                    }, "AppShutdownThread-SSE").start();
                    log.info("Application shutdown thread started via SSE /destroy. SSE stream completed.");

                } else {
                    log.error(
                            "ApplicationContext is not available. Cannot programmatically shut down the application (SSE).");
                    sendSseEvent(emitter, "log", "ERROR: ApplicationContext not found. Shutdown failed.");
                    emitter.completeWithError(
                            new IllegalStateException("ApplicationContext not found, cannot shut down."));
                }

            } catch (Exception e) {
                log.error("Error in SSE task for destroyApplication", e);
                try {
                    sendSseEvent(emitter, "log", "Critical error in SSE task for destroy: " + e.getMessage());
                } catch (Exception ex) {
                    log.warn("Failed to send critical SSE error message to client for destroy: {}", ex.getMessage());
                }
                emitter.completeWithError(e);
            }
        });
        log.info("SseEmitter returned to client for /destroy endpoint.");
        return emitter;
    }

    @Override
    public void destroy() throws Exception {
        log.info("=== DESTROYING RECORDING CONTROLLER ===");
        log.info("Shutting down RecordingController. Cleaning up resources...");
        log.info("Current active tasks at destroy time: {}", activeTaskIds);

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("ExecutorService did not terminate within 60 seconds, forcing shutdown...");
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("ExecutorService did not terminate even after forced shutdown.");
                }
            } else {
                log.info("ExecutorService terminated successfully.");
            }
        } catch (InterruptedException ie) {
            log.warn("Interrupted while waiting for ExecutorService termination, forcing shutdown...");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        destroyAgoraService();

        log.info("RecordingController cleanup finished successfully.");
    }

    private void destroyAgoraService() {
        if (recordingManager != null) {
            recordingManager.destroy();
            log.info("RecordingManager destroyed successfully.");
        }

        AgoraServiceInitializer.destroy();
        log.info("AgoraServiceInitializer resources released successfully.");
    }
}