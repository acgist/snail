@echo off

set project=snail
set version=1.0.1
set builder=.\builder\
set lib=lib
set logs=logs
set runtime=java
set database=database
set jar=%project%-%version%.jar
set exe=SnailLauncher.exe
set ini=snail.ini
set launcher=.\SnailLauncher\%exe%
set launcherIni=.\SnailLauncher\%ini%
set modules="java.sql,java.xml,java.base,java.naming,java.desktop,java.logging,java.net.http,java.scripting,java.management,jdk.unsupported"