#!/bin/sh

# 加载配置文件
source ./config.sh

echo "开始构建项目【${project}】"

# 确认版本信息
read -r -p "请确认所有配置文件（pom.xml、SnailLauncher/snail.ini、builder/config.bat、src/main/resources/config/system.properties）版本信息一致（Y/N）？" input
case $input in
  [yY])
    echo "-----------------------------------------------"
    echo "开始构建项目"
    echo "-----------------------------------------------"
    ;;

  *)
    echo "退出"
    exit 1
    ;;
esac

# 清除文件
source ./clean.sh

cd ../

echo "-----------------------------------------------"
echo "打包项目"
echo "-----------------------------------------------"
#mvn clean package -q -P release -D skipTests

echo "-----------------------------------------------"
echo "拷贝文件"
echo "-----------------------------------------------"
mkdir -p $target$lib
cp ./target/${lib}/* ${target}${lib}/
cp ./target/${jar} $target
cp $builder$config $target$config
cp $builder$startup $target$startup

echo "-----------------------------------------------"
echo "运行环境"
echo "-----------------------------------------------"
jlink --add-modules $modules --output $target$runtime

cd $builder

echo "-----------------------------------------------"
echo "构建成功"
echo "-----------------------------------------------"

exit 0
