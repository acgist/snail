# SnailLauncer

SnailLauncher是Windows（x64）系统下蜗牛（Snail）启动器

执行文件下载：[SnailLauncher.exe](https://gitee.com/acgist/snail/attach_files)

## 说明

启动器基于MFC开发，项目可以使用命令启动，启动器仅仅提供一个启动快捷方式。

## 编译

#### 依赖环境

* [CMake](https://cmake.org/)
* [Visual Studio](https://visualstudio.microsoft.com/zh-hans/vs/)

> 建议执行`builder/build.bat`命令进行编译

#### 编译命令

```bash
# 创建目录
mkdir build
cd build
# 构建项目
cmake -G "Visual Studio 11 2012 Win64" ..
# 编译项目
cmake --build . --config Release
```

> 编译前请修改`src/CMakeLists.txt`配置中JVM头文件路径