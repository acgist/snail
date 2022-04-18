#! /bin/bash

# 配置参数
action=$3
system=$2
version=$1

# 判断条件
if [[ $version == "" || $system == "" || $action == "" ]]; then
    echo "setting version and system：build.sh 1.0.0 [win|mac|linux] [all|build|pack]"
    exit
fi

# 系统参数
if [[ $system == "win" ]]; then
    # win:msi|exe
    suffix="msi"
    icon="./docs/logo/logo.ico"
    args="--win-shortcut --win-dir-chooser --win-per-user-install"
elif [[ $system == "mac" ]]; then
    # mac:pkg|dmg
    suffix="pkg"
    icon="./docs/logo/logo.icns"
    args=""
elif [[ $system == "linux" ]]; then
    # linux:deb|rpm
    suffix="deb"
    icon="./docs/logo/logo.png"
    args="--linux-shortcut"
else
    echo "setting version and system：build.sh 1.0.0 [win|mac|linux] [all|build|pack]"
    exit
fi

# 开始构建
echo "Snail $version $system building..."

# 编译项目
if [[ $action == "all" || $action == "build" ]]; then
    # 删除文件
    mvn clean
    rm -rf ./build/
    # 编译项目
    mvn package -P release -D javafx.platform=$system -D skipTests
    mkdir -p ./build/snail/
    # 拷贝文件
    cp -vr ./snail-javafx/target/lib ./build/snail/
    cp -vr ./snail-javafx/target/snail.javafx-${version}.jar ./build/snail/
    tar -cvf ./build/snail-${system}-${version}-without-jre.tar -C ./build/ snail
fi

# 打包项目：jlink|jpackage
# -XX:+UseG1GC -XX:+UseStringDeduplication
if [[ $action == "all" || $action == "pack" ]]; then
    jpackage \
        --name snail \
        --icon "${icon}" \
        --type ${suffix} \
        --input ./build/snail/ \
        --vendor acgist \
        --about-url "https://gitee.com/acgist/snail" \
        --copyright "Copyright (C) 2019 acgist. All Rights Reserved." \
        --description "Acgist Snail Downloader" \
        --app-version ${version} \
        --license-file ./LICENSE \
        --file-associations ./docs/associations/torrent.properties \
        --main-jar snail.javafx-${version}.jar \
        --add-modules "java.base,java.xml,java.desktop,java.scripting,jdk.unsupported" \
        --java-options "-server -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -Dfile.encoding=UTF-8" \
        --dest ./build/ \
        --verbose \
        ${args}
fi

echo "done"
