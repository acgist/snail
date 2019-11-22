@echo off

rem 软件名称
set project=snail
rem 软件版本
set version=1.2.1

rem 编译路径
set builder=.\builder\
rem 打包目录
set target=%builder%%project%\

rem 依赖包文件夹
set lib=lib
rem 运行环境文件夹
set runtime=java

rem JAR文件
set jar=%project%-%version%.jar

rem BAT配置文件
set config=config.bat
rem BAT启动文件
set startup=startup.bat

rem 启动文件
set exe=SnailLauncher.exe
rem 启动文件配置
set ini=snail.ini
rem 启动文件路径
set launcherExe=.\SnailLauncher\%exe%
rem 启动文件配置路径
set launcherIni=.\SnailLauncher\%ini%

rem JAVA依赖模块
set modules="java.xml,java.sql,java.base,java.desktop,java.naming,java.compiler,java.logging,java.scripting,java.instrument,java.management,java.net.http,java.transaction.xa,jdk.crypto.ec,jdk.unsupported"
