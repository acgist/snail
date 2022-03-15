# 使用帮助

如果遇到其他问题或建议请提交[Issues](https://gitee.com/acgist/snail/issues)

## 目录

* [安全](#安全)
 * [数据](#数据)
 * [隐私](#隐私)
* [稳定版本](#稳定版本)
* [使用](#使用)
 * [环境](#环境)
 * [编译](#编译)
 * [打包](#打包)
 * [乱码](#乱码)
 * [运行](#运行)
 * [统计](#统计)
 * [进阶](#进阶)
* [视频播放](#视频播放)
* [BT任务](#bt任务)
 * [BT任务速度](#bt任务速度)
 * [BT任务分享](#bt任务分享)
 * [BT任务无法分享](#bt任务无法分享)
 * [BT任务无法下载](#bt任务无法下载)
 * [BT文件校验](#bt文件校验)
 * [BT文件修复](#bt文件修复)
* [私有种子](#私有种子)
* [磁力链接任务](#磁力链接任务)
 * [磁力链接任务原理](#磁力链接任务原理)
 * [磁力链接任务无法下载](#磁力链接任务无法下载)
* [内网穿透](#内网穿透)
 * [内网穿透原理](#内网穿透原理)
 * [UPNP映射失败原因](#upnp映射失败原因)
* [优化下载体验](#优化下载体验)
 * [自定义Tracker服务器](#自定义tracker服务器)
 * [自定义DHT网络](#自定义dht网络)
* [常见问题](#常见问题)
 * [内存溢出](#内存溢出)
 * [下载一段时间没有速度](#下载一段时间没有速度)
 * [启动后没有出现GUI界面](#启动后没有出现gui界面)
* [开发帮助](#开发帮助)

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

Snail基于Java开发所以支持多种平台：

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

使用JDK内置命令`jpackage`进行打包，可以使用[docs/build.sh](./build.sh)脚本打包：

```bash
./docs/build.sh version [win|mac|linux] [all|build|pack]
```

##### 注意

* 打包命令需要安装正确Java环境
* 参数`pack`可选：是否打包

##### Windows

可以在Git Bash命令行里面执行

##### Ubuntu

提示缺少fakeroot：`sudo apt install alien`

##### CentOS

提示错误：类型[rpm]无效或不受支持：`yum install rpm-build redhat-rpm-config`

### 乱码

* Windows控制台乱码设置编码：`chcp 65001`
* 添加启动参数：`-D file.encoding=UTF-8`

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

BT任务需要用户提供种子文件（.torrent）

### BT任务速度

BT任务的速度会随着查找到的Peer数量增加慢慢增加

> 商业软件由于用户数量庞大，拥有自己的离线服务器，还有一些特殊协议和规则，所以速度比较快。

> Snail完全依赖公共资源和用户做种进行下载，当然Snail也会更加努力变得越来越快。

### BT任务分享

BT任务上传速度是下载速度的四分之一，任务只要开始下载都会进行分享，直到软件关闭或者任务被删除。

### BT任务无法分享

* 接入多条网络
* 处于多重路由网络环境
* 路由没有开启UPNP功能
* [内网穿透](#内网穿透)设置失败

### BT任务无法下载

* 网络故障
* 种子损坏
* 无人分享（[做种](./GUI.md#来源统计)）
* 任务文件不完整（[健康度](./GUI.md#piece统计)）
* 没有可用的[Tracker服务器](./GUI.md#tracker统计)和[DHT网络节点](./GUI.md#节点统计)

### BT文件校验

BT文件是分片下载的，部分任务会存在一个分片里面包含多个文件，这样的分片如果文件不是同时下载校验时就需要下载不需要下载的文件。
这样的文件大多数客户端都不会进行下载，导致文件没有数据分享然后导致下载卡住，所以蜗牛选择忽略这样的分片数据校验。

> 忽略分片都是文件第一片和最后一片

### BT文件修复

由于不可抗力因素（断电、软件崩溃、系统崩溃）导致任务没有正常结束，重启软件然后右键任务选择**文件校验**修复下载任务。

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

磁力链接下载使用Tracker服务器和DHT网络先加载Peer，然后连接Peer进行种子交换，最后转为BT任务进行下载。

### 磁力链接任务无法下载

* 磁力链接错误
* 系统没有配置Tracker服务器和DHT网络

> 磁力链接在转种子过程中会出现长时间的等待（查找和连接Peer）

## 内网穿透

内网穿透主要为了实现外部Peer连接客户端、分享资源、加速下载

### 内网穿透原理

|协议|传输协议|
|:--|:--:|
|UPNP|TCP、UDP|
|STUN|UDP|

优先使用UPNP进行端口映射，如果映射失败再使用STUN实现穿透。

### UPNP映射失败原因

* 处于多重路由网络环境
* 路由没有开启UPNP功能
* 系统没有自动获取IP（电脑配置固定IP）
* 其他错误原因

#### 通过UPNP进行TCP内网穿透

```java
UpnpClient.newInstance().mSearch();
NatContext.getInstance().lock();
if(UpnpContext.getInstance().available()) {
	UpnpContext.getInstance().addPortMapping(8080, 8080, Protocol.Type.TCP);
}
```

## 优化下载体验

* 使用热门种子
* 自定义优质的Tracker服务器和DHT网络节点

### 自定义Tracker服务器

在软件根目录文件`/config/bt.tracker.properties`配置键值对：`index=AnnounceUrl`

> index=任意值

> AnnounceUrl=Tracker服务器地址

优质Tracker服务器：[trackerslist](https://github.com/ngosang/trackerslist)

### 自定义DHT网络

在软件根目录文件`/config/bt.dht.properties`配置键值对：`NodeID=host:port`

> NodeID=节点ID

> host:port=节点地址（域名|IP）:端口

## 常见问题

### 内存溢出

使用FTP、HLS、HTTP下载大文件时如果出现内存溢出，建议优化JVM参数：`-XX:NewRatio=2 -XX:SurvivorRatio=2`

```bash
java -server -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -jar snail.javafx-{version}.jar
```

或者调小磁盘缓存

### 下载一段时间没有速度

如果出现开始下载有速度，下载一段时间后没有速度了，可以先暂停任务或重启软件再开始下载。

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