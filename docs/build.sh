#! /bin/bash

# 版本
version=$1
# 系统：win(exe/msi)、mac(pkg/dmg)、linux(rpm/deb)
os=$2

echo "Snail $version $os building..."

# 删除文件

#mvn clean
#rm -rf "./build/"

# 打包项目

#mvn clean package -P release -D skipTests

# 打包环境

#jlink --add-modules "java.base,java.xml,java.desktop,java.scripting,jdk.unsupported" --output "./build/runtime"

# 拷贝文件
mkdir -p "./build/snail/"
cp -v "./LICENSE" "./build/"
cp -vr "./snail-javafx/target/lib" "./build/snail/"
cp -v "./snail-javafx/src/main/resources/image/logo.png" "./build/"
cp -v "./snail-javafx/target/snail.javafx-${version}.jar" "./build/snail/"

# 打包
#call start "snail" javaw -server "-Dfile.encoding=UTF-8" -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -jar %jar%