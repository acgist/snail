@echo off

rem 加载配置文件
call config.bat

echo 开始构建项目【%project%】

set /p input=请确认配置文件版本信息是否一致（Y/N）？
if /i %input%==Y (
  echo -----------------------------------------------
  echo 构建项目
  echo -----------------------------------------------
) else (
  exit
)

rem 清除文件
call clean.bat

cd ..\

echo -----------------------------------------------
echo 编译项目
echo -----------------------------------------------
call mvn clean package -P release -D skipTests

echo -----------------------------------------------
echo 运行环境
echo -----------------------------------------------
call jlink --add-modules %modules% --output %target%%runtime%

echo -----------------------------------------------
echo 编译启动器
echo -----------------------------------------------
cd %launcher%
call mkdir build
call cd build
call cmake -G "Visual Studio 11 2012 Win64" ..
call cmake --build . --config Release

cd ..\..\

echo -----------------------------------------------
echo 拷贝文件
echo -----------------------------------------------
call xcopy /S /Q .\snail-javafx\target\%lib%\* %target%%lib%\
call copy .\snail-javafx\target\%jar% %target%
call copy %launcherExe% %target%%exe%
call copy %launcherIni% %target%%ini%
call copy %builder%%config% %target%%config%
call copy %builder%%startup% %target%%startup%

cd %builder%

echo -----------------------------------------------
echo 构建成功
echo -----------------------------------------------

exit