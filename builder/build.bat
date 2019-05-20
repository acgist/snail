@echo off

rem 加载配置文件
call config.bat

echo 开始构建项目【%project%】
echo 请确保pom.xml、snail.ini、config.bat、system.properties配置文件中版本号一致

rem 清除文件
call clean.bat

cd ..\

echo -----------------------------------------------
echo 打包项目
echo -----------------------------------------------
call mvn clean package -q -Prelease -DskipTests

echo -----------------------------------------------
echo 拷贝文件
echo -----------------------------------------------
call xcopy /S /Q .\target\%lib%\* %target%%lib%\*
call copy .\target\%jar% %target%
call copy %launcherExe% %target%%exe%
call copy %launcherIni% %target%%ini%
call copy %builder%%config% %target%%config%
call copy %builder%%startup% %target%%startup%

echo -----------------------------------------------
echo 运行环境
echo -----------------------------------------------
call jlink --add-modules %modules% --output %target%%runtime%

cd %builder%

echo -----------------------------------------------
echo 构建成功
echo -----------------------------------------------
