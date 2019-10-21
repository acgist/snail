#!/bin/sh

# 加载配置文件
source ./config.sh

echo "启动项目【${project}】"

# 设置环境变量
PATH="./${runtime}/bin"

# 启动命令
java -server -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -jar $jar

exit 0
