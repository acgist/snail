#! /bin/bash

# 配置参数

pack=$3
system=$2
version=$1

if [[ $version == "" ]] || [[ $system == "" ]]; then
    echo "setting version and system：build.sh 1.0.0 [win|mac|linux] [pack]"
    exit
fi

# 系统参数

if [[ $system == "win" ]]; then
    # win:msi|exe
    icon="./docs/logo/logo.ico"
    args="--type msi --win-shortcut --win-dir-chooser --win-per-user-install"
elif [[ $system == "mac" ]]; then
    # mac:pkg|dmg
    icon="./docs/logo/logo.icns"
    args="--type pkg"
elif [[ $system == "linux" ]]; then
    # linux:deb|rpm
    icon="./docs/logo/logo.png"
    args="--type deb --linux-shortcut"
else
    echo "setting version and system：build.sh 1.0.0 [win|mac|linux] [pack]"
    exit
fi

# 开始构建

echo "Snail $version $system building..."

# 删除文件

mvn clean
rm -rf ./build/

# 编译项目

mvn package -P release -D javafx.platform=$system -D skipTests
mkdir -p ./build/snail/
cp -vr ./snail-javafx/target/lib ./build/snail/
cp -vr ./snail-javafx/target/snail.javafx-${version}.jar ./build/snail/

# 打包项目

tar -cvf ./build/snail-${system}-${version}-without-jre.tar -C ./build/ snail

if [[ $pack == "pack" ]]; then
# 运行环境
    jlink --add-modules "java.base,java.xml,java.desktop,java.scripting,jdk.unsupported" --output ./build/runtime
# 打包软件
    jpackage \
        --name snail \
        --app-version ${version} \
        --vendor acgist \
        --copyright "Copyright (C) 2019 acgist. All Rights Reserved." \
        --description "Acgist Snail Downloader" \
        --input ./build/snail/ \
        --main-jar snail.javafx-${version}.jar \
        --runtime-image ./build/runtime \
        --icon ${icon} \
        --license-file ./LICENSE \
        --file-associations ./docs/torrent.properties \
        --java-options "-server -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -Dfile.encoding=UTF-8" \
        --dest ./build/ \
        ${args}
fi

echo "done"
