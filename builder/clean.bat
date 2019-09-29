@echo off

rem 加载配置文件
call config.bat

rem 用户确认
set /p input=请确认清除已生成的文件（Y/N）？
if /i %input%==Y (echo 开始清除) else (exit)

echo -----------------------------------------------
echo 清除文件
echo -----------------------------------------------
if exist %project% rd /S /Q %project%
