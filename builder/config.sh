#!/bin/sh

# 软件名称
project=snail
# 软件版本
version=1.2.0

# 编译路径
builder=./builder/
# 打包目录
target="${builder}${project}/"

# 依赖包文件夹
lib=lib
# 运行环境文件夹
runtime=java

# JAR文件
jar="${project}-${version}.jar"

# SH配置文件
config=config.sh
# SH启动文件
startup=startup.sh

# JAVA依赖模块
modules="java.xml,java.sql,java.base,java.desktop,java.naming,java.compiler,java.logging,java.scripting,java.instrument,java.management,java.net.http,java.transaction.xa,jdk.crypto.ec,jdk.unsupported"
