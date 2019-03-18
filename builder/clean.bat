@echo off

call config.bat

rem 用户确认
set /p input=请确认删除全部内容（Y/N）？
if /i %input%==Y (echo 开始删除) else (exit)

rem 删除文件
echo -----------------------------------------------
echo 删除文件
echo -----------------------------------------------
if exist %jar% del /F /A /Q %jar%
if exist %exe% del /F /A /Q %exe%
if exist %lib% rd /S /Q %lib%
if exist %logs% rd /S /Q %logs%
rem if exist %database% rd /S /Q %database%
if exist %runtime% rd /S /Q %runtime%