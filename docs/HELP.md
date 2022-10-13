# 使用帮助

如果遇到其他问题或建议请提交[Issues](https://gitee.com/acgist/snail/issues)

## 安全

### 数据

* 除了下载任务以外不会采集其他任何信息
* 除了用于必要的下载和上传任务而进行数据传输外不会有其他任何的数据传输

### 隐私

*谁可能知道你的下载任务？*

* 运营商
* 连接的Peer
* 连接的DHT节点
* 连接的FTP服务器
* 连接的HTTP服务器
* 连接的Tracker服务器
* 局域网内的本地发现服务

## 稳定版本

[最新稳定版本下载](https://gitee.com/acgist/snail/attach_files)

### 附件说明

|文件|描述|
|:--|:--|
|snail-x.x.x.msi|Win安装版|
|snail-x.x.x.pkg|Mac安装版|
|snail-x.x.x.deb|Linux安装版|
|snail-win-x.x.x-without-jre.tar|Win绿色版|
|snail-mac-x.x.x-without-jre.tar|Mac绿色版|
|snail-linux-x.x.x-without-jre.tar|Linux绿色版|

* 版本：`snail-主版本号.Java版本号.修订号`

* 带有`-without-jre`绿色版需要自己安装`Java`运行环境

## 使用

`Snail`基于`Java`开发所以支持多种平台：

* Mac
* Linux：CentOS/Ubuntu
* Windows：7/10

> 上面系统经过运行下载测试

### 环境

[![Java](https://img.shields.io/badge/dynamic/xml?style=flat-square&label=Java&url=https://raw.githubusercontent.com/acgist/snail/master/pom.xml&query=//*[local-name()='java.version']&cacheSeconds=3600)](http://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6.0+-007EC6?style=flat-square)](https://maven.apache.org/)
![系统](https://img.shields.io/badge/系统-win%20%7C%20mac%20%7C%20linux-007EC6?style=flat-square)

### 编译

```bash
mvn clean package -P release -D skipTests
```

> 编译系统和运行系统不一致时添加编译参数：`-D javafx.platform=win|mac|linux`

```bash
mvn clean package -P release -D skipTests -D javafx.platform=win|mac|linux
```

> 推荐使用[Release](https://gitee.com/acgist/snail/releases)编译

### 打包

使用JDK内置命令`jpackage`进行打包，现已提供打包脚本[docs/build.sh](./build.sh)，命令如下：

```bash
./docs/build.sh version [win|mac|linux] [all|build|pack]
```

##### 注意

* 打包命令需要安装正确`Java`环境

##### Windows

可以在`Git Bash`命令行里面执行

##### Ubuntu

提示缺少`fakeroot`：`sudo apt install alien`

##### CentOS

提示错误：类型`[rpm]`无效或不受支持：`yum install rpm-build redhat-rpm-config`

### 乱码

* 添加启动参数：`-D file.encoding=UTF-8`
* `Windows`控制台乱码设置编码：`chcp 65001`

### 运行

```bash
java -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar
```

> Windows可以使用`javaw`命令

### 统计

统计页面信息关闭软件后不会记录，所以部分信息不会累计。

### 进阶

* 问题建议：`F1`
* 统计面板：`F12`
* 下载界面、新建下载界面支持拖拽操作：下载链接、种子文件

## 视频播放

蜗牛专注下载，不会实现播放功能。

### 技巧

虽然没有实现播放功能，但是理论上下载是按序下载，所以已下载的部分可以直接使用播放器进行播放。

> 可以通过`F12`查看已下载的部分

### 边下边播

[指定优先下载位置](./GUI.md#piece统计)

## BT任务

BT任务需要用户提供种子文件（`.torrent`）

### BT任务速度

BT任务的速度会随着查找到的Peer数量增加慢慢增加

### BT任务分享

BT任务上传速度是下载速度的四分之一，任务只要开始下载都会进行分享，直到软件关闭或者任务被删除。

### BT任务无法分享

* 接入多条网络
* 处于多重路由网络环境
* [内网穿透](#内网穿透)设置失败

### BT任务无法下载

* 网络故障
* 种子损坏
* 无人分享（[做种](./GUI.md#peer来源统计)）
* 任务文件不完整（[健康度](./GUI.md#piece统计)）
* 没有可用的[Tracker服务器](./GUI.md#tracker统计)和[DHT网络节点](./GUI.md#dht节点统计)

### BT文件校验

BT任务文件都是分片下载的，分片大小都是固定的，所以就会存在部分文件开始和结束所处分片里面包含多个文件的情况，如果这些分片相关文件没有选择全部下载，校验时就需要下载不需要下载的文件。
然而这样的文件大多数客户端都不会进行下载，进而导致文件没有客户端提供数据分享，最终导致下载卡住，所以蜗牛选择直接忽略这些分片的数据校验。

> 忽略分片都是文件开始和结束所处的分片

### BT文件修复

由于不可抗力因素（断电、软件崩溃、系统崩溃）导致任务没有正常结束，重启软件然后右键任务选择**文件校验**即可修复下载任务。

* 文件校验非常消耗性能不建议按着玩
* 文件校验要在任务开始下载后才有效

## 私有种子

私有种子下载支持以下特性：

* 不使用PEX协议
* 不使用DHT网络
* 不使用本地发现服务
* 只使用种子自带Tracker服务器

## 磁力链接任务

磁力链接任务需要用户提供磁力链接：32位磁力链接HASH、40位磁力链接HASH、完整磁力链接

### 磁力链接任务原理

磁力链接下载通过Tracker服务器和DHT网络先加载Peer，然后连接Peer进行种子交换，最后转为BT任务进行下载。

### 磁力链接任务无法下载

* 磁力链接错误
* 没有可用的[Tracker服务器](./GUI.md#tracker统计)和[DHT网络节点](./GUI.md#dht节点统计)

> 磁力链接在转种子过程中会出现长时间的等待（查找和连接Peer）

## 内网穿透

内网穿透主要为了实现外部Peer连接客户端、分享资源、加速下载

### 内网穿透原理

|协议|传输协议|
|:--|:--:|
|UPNP|TCP、UDP|
|STUN|UDP|

> 优先使用UPNP进行端口映射，如果映射失败再使用STUN实现穿透。

### UPNP映射失败原因

* 处于多重路由网络环境
* 路由没有开启UPNP功能
* 系统没有自动获取IP（电脑配置固定IP）

## 优化下载体验

* 使用热门种子
* 自定义优质的Tracker服务器和DHT网络节点

### 自定义Tracker服务器

在软件根目录文件`/config/bt.tracker.properties`配置键值对：`index=AnnounceUrl`

> 优质Tracker服务器：[trackerslist](https://github.com/ngosang/trackerslist)

### 自定义DHT网络

在软件根目录文件`/config/bt.dht.properties`配置键值对：`NodeID=host:port`

## 常见问题

### 内存溢出

使用`FTP`、`HLS`、`HTTP`下载大文件时如果出现内存溢出，建议优化JVM参数：`-XX:NewRatio=2 -XX:SurvivorRatio=2`或者调小磁盘缓存

```bash
java -server -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -jar snail.javafx-{version}.jar
```

### 下载一段时间没有速度

如果出现开始下载有速度，下载一段时间后没有速度了，可以尝试先暂停任务或重启软件再开始下载。

### 启动后没有出现GUI界面

* 可以查看端口`16888`和`18888`是否占用：

```bash
# Linux
# 查询端口
netstat -anp | grep 16888
netstat -anp | grep 18888
# 查询占用程序
ps aux | grep pid

#Windows
# 查询端口
netstat -ano | findstr 16888
netstat -ano | findstr 18888
# 查询占用程序
tasklist | findstr pid
```

> pid：查询进程ID

* 端口未被占用可以试试使用管理用户权限运行

## 开发帮助

[开发帮助](./API.md)