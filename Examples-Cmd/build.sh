#!/bin/bash
set -x
set -e

rm -fr build
mkdir -p build

find src -name "*.java" >build/test_source.txt

# 编译 Java 文件
javac -g -cp .:third_party/log4j-api-2.24.3.jar:third_party/log4j-core-2.24.3.jar:third_party/gson-2.11.0.jar:libs/agora-recording-sdk.jar -encoding utf-8 @build/test_source.txt -d build -XDignore.symbol.file

echo "Build completed successfully"
