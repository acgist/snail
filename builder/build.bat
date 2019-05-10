@echo off

call config.bat

echo 开始构建项目【%project%】

call clean.bat

cd ..\

rem 打包项目
echo -----------------------------------------------
echo 打包项目
echo -----------------------------------------------
call mvn clean package -q -Prelease -DskipTests
call xcopy /S /Q .\target\%lib%\* %builder%%lib%\*
call copy .\target\%jar% %builder%
call copy %launcher% %builder%%exe%

rem 生成JAVA运行环境
rem 查询依赖命令：jdeps --list-deps *.jar
echo -----------------------------------------------
echo 生成JAVA运行环境
echo -----------------------------------------------
call jlink --add-modules %modules% --output %builder%%runtime%

cd %builder%