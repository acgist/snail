# SnailLauncer

SnailLauncher是Windows（x64）系统下蜗牛（Snail）启动器

执行文件下载：[SnailLauncher.exe](https://gitee.com/acgist/snail/attach_files)

## 说明

基于MFC开发

## 编译

需要安装以下软件：

- [CMake](https://cmake.org/)
- [Visual Studio](https://visualstudio.microsoft.com/zh-hans/vs/)

> 建议执行`builder/build.bat`命令进行编译

```bash
# 构建目录
mkdir build
cd build
# 构建项目
cmake -G "Visual Studio 11 2012 Win64" ..
# 编译项目
cmake --build . --config Release
```
