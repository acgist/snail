@echo off

call config.bat

echo 开始构建项目%project%

cd ..\

rem 删除旧文件
echo -----------------------------------------------
echo 删除旧文件
echo -----------------------------------------------
del /F /A /Q %builder%%jar%
rd /S /Q %builder%%runtime%

rem 打包JAR
echo -----------------------------------------------
echo 打包JAR
echo -----------------------------------------------
call mvn clean package -DskipTests
call copy .\target\%jar% %builder%

rem 生成JAVA运行环境
rem 查询依赖命令：jdeps --list-deps *.jar
echo -----------------------------------------------
echo 生成JAVA运行环境
echo -----------------------------------------------
call jlink --add-modules "java.sql,java.base,java.desktop,java.instrument,java.xml,java.rmi,java.prefs,java.naming,java.logging,java.scripting,java.management,java.sql.rowset,java.datatransfer,java.transaction.xa,jdk.jdi,jdk.attach,jdk.httpserver,jdk.unsupported" --output %builder%%runtime%

cd %builder%