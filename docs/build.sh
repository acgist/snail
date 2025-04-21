#! /bin/bash

info() {
  echo ""
  echo "--------------------------------"
  echo ""
  echo "$1"
  echo ""
  echo "--------------------------------"
  echo ""
}

fail() {
  echo ""
  echo "--------------------------------"
  echo ""
  echo -e "\033[31m$1\033[0m"
  echo ""
  echo "--------------------------------"
  echo ""
}

success() {
  echo ""
  echo "--------------------------------"
  echo ""
  echo -e "\033[32m$1\033[0m"
  echo ""
  echo "--------------------------------"
  echo ""
}

# 配置参数
action=$3
system=$2
version=$1

# 判断参数
if [[ $version == "" || $system == "" || $action == "" ]]; then
  fail "Usage: build.sh 1.0.0 [win|mac|linux] [all|pack|build]"
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
  fail "unknown system：$system"
  exit
fi

# 开始构建
info "Snail building $version $system $action"

# 编译项目
if [[ $action == "all" || $action == "pack" || $action == "build" ]]; then
  info "Snail run maven";
  # 删除文件
  mvn clean
  rm -rf ./build/
  # 编译项目
  mvn package dependency:copy-dependencies -P release -D skipTests -D javafx.platform=$system
  mkdir -p ./build/snail/
  # 拷贝文件
  cp -vr ./snail-javafx/target/lib ./build/snail/
  cp -vr ./snail-javafx/target/snail.javafx-${version}.jar ./build/snail/
  tar -cvf ./build/snail-${system}-${version}-without-jre.tar -C ./build/ snail
fi

# 打包项目：jlink|jpackage
if [[ $action == "all" || $action == "pack" ]]; then
  info "Snail run jpackage";
  jpackage \
    --name snail \
    --icon ${icon} \
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

if [[ $? -eq 0 ]] ; then
  success "打包完成";
else
  fail "打包失败";
fi
