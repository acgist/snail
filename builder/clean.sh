#!/bin/sh

# 加载配置文件
source ./config.sh

# 用户确认
read -r -p "请确认清除已生成的文件（Y/N）？" input
case $input in
  [yY])
    echo "-----------------------------------------------"
    echo "清除文件"
    echo "-----------------------------------------------"
    ;;

  *)
    echo "退出"
    exit 1
    ;;
esac

if [[ -a $project ]];then
  rm -rf $project
fi;
