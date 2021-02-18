#! /bin/bash

# 配置参数

os=$2
pack=$3
version=$1

if [[ $version == "" ]] || [[ $os == "" ]]; then
    echo "setting version and os：build.sh 1.0.0 [win|mac|linux] [pack]"
    exit
fi

echo "Snail $version $os building..."

# 删除文件

mvn clean
rm -rf "./build/"

# 编译项目

mvn package -P release -D "javafx.platform=$os" -D skipTests
mkdir -p "./build/snail/"
cp -v "./LICENSE" "./build/"
cp -v "./docs/logo.ico" "./build/"
cp -vr "./snail-javafx/target/lib" "./build/snail/"
cp -v "./snail-javafx/target/snail.javafx-${version}.jar" "./build/snail/"

# 打包项目

if [[ $pack == "pack" ]]; then
# 运行环境
    jlink --add-modules "java.base,java.xml,java.desktop,java.scripting,jdk.unsupported" --output "./build/runtime"
# 系统参数
    if [[ $os == "win" ]]; then
#       win:msi|exe
        args="--type msi --win-shortcut --win-dir-chooser"
    elif [[ $os == "mac" ]]; then
#       mac:pkg|dmg
        args="--type pkg"
    elif [[ $os == "linux" ]]; then
#       linux:rpm|deb
        args="--type rpm --linux-shortcut"
    else
        echo "unknown os"
    fi
# 打包软件
    jpackage \
        --name snail \
        --app-version ${version} \
        --vendor acgist \
        --copyright acgist \
        --description "acgist snail" \
        --input "./build/snail/" \
        --main-jar "snail.javafx-${version}.jar" \
        --runtime-image "./build/runtime" \
        --icon "./build/logo.ico" \
        --license-file "./build/LICENSE" \
        --java-options "-server -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -Dfile.encoding=UTF-8" \
        --dest "./build/" \
        $args
fi

echo "done"
