@echo off

call config.bat

rem 删除文件
echo -----------------------------------------------
echo 删除文件
echo -----------------------------------------------
if exist %jar% del /F /A /Q %jar%
if exist %lib% rd /S /Q %lib%
if exist %logs% rd /S /Q %logs%
if exist %runtime% rd /S /Q %runtime%