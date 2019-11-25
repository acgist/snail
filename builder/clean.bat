@echo off

rem 加载配置文件
call config.bat

rem 用户确认
set /p input=请确认是否清除已生成文件（Y/N）？
if /i %input%==Y (echo 开始清除) else (exit)

echo -----------------------------------------------
echo 清除文件
echo -----------------------------------------------
if exist %project% rd /S /Q %project%

cd ..

echo -----------------------------------------------
echo 清除MAVEN
echo -----------------------------------------------
call mvn clean -D skipTests

echo -----------------------------------------------
echo 清除启动器
echo -----------------------------------------------
if exist %launcherBuild% rd /S /Q %launcherBuild%

cd %builder%