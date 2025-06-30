#!/usr/bin/env zsh

ARCH=$(uname -m)

LIB_PATH="libs/native/linux/$ARCH"

export DYLD_LIBRARY_PATH=$(pwd)/$LIB_PATH
export LD_LIBRARY_PATH=$(pwd)/$LIB_PATH
echo "$LD_LIBRARY_PATH"

CLASS=$1
shift 1

echo "Parameters passed to run.zsh: $@"

# Define common Java runtime parameters
DEBUG_OPTS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -Xcheck:jni -XX:NativeMemoryTracking=detail -XX:+PrintCommandLineFlags"
CRASH_OPTS="-XX:ErrorFile=./logs/hs_err_pid%p.log -XX:LogFile=./logs/jvm.log -XX:+CreateMinidumpOnCrash -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/"
JAVA_OPTS="$DEBUG_OPTS $CRASH_OPTS"

# Set core dump related configuration
ulimit -c unlimited
# Create core file directory
mkdir -p ./logs
chmod 777 ./logs

# Set system core dump behavior
#cd ./logs
#CORE_PATH=$(pwd)
#cd ..
#echo "core dump path: $CORE_PATH"
# Set core file save path
#echo "$CORE_PATH/core.%e.%p" >/proc/sys/kernel/core_pattern

# Add signal handling related options
export JAVA_OPTS="$JAVA_OPTS -XX:+UseSignalChaining"

# Memory debugging options
# export MALLOC_CHECK_=7
# export MALLOC_PERTURB_=42

# Unset environment variables that may cause issues
unset _JAVA_OPTIONS
unset JAVA_TOOL_OPTIONS

# Set additional system limits
ulimit -s unlimited # Set stack size to unlimited

CLASSPATH=".:third_party/gson-2.11.0.jar:third_party/log4j-api-2.24.3.jar:third_party/log4j-core-2.24.3.jar:./libs/agora-recording-sdk.jar:./build"

# Check if ASAN is enabled
if [ "$last_arg" = "-asan" ]; then
    # Set ASAN related environment variables
    export LD_PRELOAD=/usr/lib/gcc/x86_64-linux-gnu/13/libasan.so
    export ASAN_OPTIONS=detect_container_overflow=0
fi

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Ensure Java library path includes all necessary directories
JAVA_LIBRARY_PATH="-Djava.library.path=libs:$LIB_PATH"

# Execute Java command
java -cp $CLASSPATH $JAVA_OPTS -Dlog.filename=app-$TIMESTAMP -Dlog4j.configurationFile=file:./log4j2.xml $JAVA_LIBRARY_PATH $CLASS $* |
    grep -v "WARNING in native method: JNI call made without checking exceptions"
