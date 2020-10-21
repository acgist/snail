欢迎使用Snail（蜗牛）

如果遇到其他问题或建议请提交[Issues](https://gitee.com/acgist/snail/issues)

## 目录

* [隐私](#隐私)
	* [你的行为](#你的行为)
* [使用帮助](#使用帮助)
	* [下载](#下载)
	* [使用](#使用)
		* [环境](#环境)
		* [编译](#编译)
		* [乱码](#乱码)
		* [运行](#运行)
		* [统计](#统计)
		* [进阶](#进阶)
	* [视频播放](#视频播放)
	* [BT任务](#bt任务)
		* [BT任务分享](#bt任务分享)
		* [BT任务无法分享](#bt任务无法分享)
		* [BT任务无法下载](#bt任务无法下载)
		* [BT文件校验](#bt文件校验)
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
* [开发帮助](#开发帮助)
	* [系统消息](#系统消息)
	* [系统通知](#系统通知)
	* [启动参数](#启动参数)
	* [启动参数](#启动参数)
	* [后台模式](#后台模式)
* [常见问题](#常见问题)
	* [内存溢出](#内存溢出)
	* [下载一段时间没有速度](#下载一段时间没有速度)
	* [启动后没有出现GUI界面](#启动后没有出现gui界面)

## 隐私

* 除了下载任务以外不会采集其他任何信息
* 除了用于必要的下载和上传任务而进行数据传输外不会有其他任何的数据传输

### 你的行为

*谁可能知道你的下载任务？*

* 运营商
* 连接的Peer
* 连接的DHT节点
* 连接的FTP服务器
* 连接的HTTP服务器
* 连接的Tracker服务器
* 局域网内的本地发现服务

## 使用帮助

### 下载

[最新稳定版本下载](https://gitee.com/acgist/snail/attach_files)（解压可以直接运行）

###### 附件说明

|文件|描述|
|:--|:--|
|SnailLauncher.exe|Windows启动器|
|SnailIcon-v1.0.zip|图标字体|
|snail-mac-vx.x.x.zip|Mac稳定版|
|snail-linux-vx.x.x.zip|Linux稳定版|
|snail-windows-vx.x.x.zip|Windows稳定版|

* 下载对应系统稳定版解压即可运行
* 不用下载Windows启动器和图标字体
* 带有`-without-jre`字样版本需要自己安装`Java`运行环境

### 使用

#### 环境

Java：11+  
Maven：3.6.0+  
系统支持：win、mac、linux

#### 编译

```bash
mvn clean package -P release -D skipTests
```

> 编译系统和运行系统不一致时添加编译参数：-D javafx.platform=win|mac|linux

```bash
mvn clean package -P release -D skipTests -D javafx.platform=win
```
#### 乱码

Windows控制台乱码设置编码：`chcp 65001`

添加启动参数：`-D file.encoding=UTF-8`

#### 运行

```bash
# Linux
java -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar

# Windows
javaw -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar
```

#### 统计

统计页面信息关闭软件后不会记录，所以部分信息不会累计。

#### 进阶

F1：问题建议

F12：统计面板

下载界面、新建下载界面支持拖拽操作：下载链接、种子文件

文件校验点击确定按钮后会自动复制校验信息到剪切板

### 视频播放

蜗牛专注下载，不会实现边下边播功能。

#### 技巧

虽然没有实现边下边播功能，但是理论上下载是按序下载，所以已下载的部分可以直接使用播放器进行播放。

> 可以通过`F12`查看已经下载的部分

### BT任务

BT任务需要用户提供种子文件（.torrent）

#### BT任务分享

BT任务上传速度是下载速度的四分之一，任务只要开始下载都会进行分享，直到软件关闭或者任务被删除。

#### BT任务无法分享

* 接入多条网络
* 处于多重路由网络环境
* 路由没有开启UPNP功能
* [内网穿透](#内网穿透)设置失败

#### BT任务无法下载

* 网络故障
* 种子损坏
* 无人分享（做种）
* 没有可用的Tracker服务器和DHT网络

#### BT文件校验

BT文件是分片下载的，部分任务会存在一个分片里面包含多个文件，这样的分片如果文件不是同时下载时校验就需要下载不需要下载的文件。
这样的文件大多数客户端可能都不会进行下载，导致文件没有数据分享就会导致下载卡住，所以蜗牛选择忽略这样的片的数据校验（文件第一片和最后一片）。

### 私有种子

私有种子下载支持以下特性：

* 只使用种子自带Tracker服务器
* 不使用PEX协议
* 不使用DHT网络
* 不使用本地发现服务

### 磁力链接任务

磁力链接任务需要用户提供磁力链接：32位磁力链接HASH、40位磁力链接HASH、完整磁力链接

#### 磁力链接任务原理

磁力链接下载使用Tracker服务器和DHT网络先加载Peer，然后连接Peer进行种子交换，最后转为BT任务进行下载。  

#### 磁力链接任务无法下载

* 磁力链接错误
* 系统没有配置Tracker服务器和DHT网络

> 磁力链接在转种子过程中会出现长时间的等待（查找和连接Peer）

### 内网穿透

内网穿透主要为了实现外部Peer连接客户端、分享资源、加速下载

#### 内网穿透原理

|协议|传输协议|
|:--|:--:|
|UPNP|TCP、UDP|
|STUN|UDP|

优先使用UPNP进行端口映射，如果映射失败再使用STUN实现穿透。

#### UPNP映射失败原因

* 处于多重路由网络环境
* 路由没有开启UPNP功能
* 系统没有自动获取IP（电脑配置固定IP）
* 其他错误原因

### 优化下载体验

* 使用热门种子
* 自定义优质的Tracker服务器和DHT网络

#### 自定义Tracker服务器

在软件根目录文件`/config/bt.tracker.properties`配置键值对：`index=AnnounceUrl`

> index=任意值

> AnnounceUrl=Tracker服务器地址

优质Tracker服务器：[trackerslist](https://github.com/ngosang/trackerslist)

#### 自定义DHT网络

在软件根目录文件`/config/bt.dht.properties`配置键值对：`NodeID=host:port`

> NodeID=节点ID

> host:port=服务器地址（域名|IP）:端口

## 开发帮助

系统消息和系统通知使用B编码Map类型消息，每条消息含有类型`type`和主体`body`。

> 通过`socket`连接系统端口`16888`发送系统消息和接收系统通知

```
d4:type4:TEXT4:body7:messagee
```

### 系统消息

通过系统消息可以实现系统管理和任务管理

|名称|类型|请求主体|响应主体|
|:--|:--|:--|:--|
|GUI注册|GUI|-|SUCCESS/失败原因|
|文本消息|TEXT|文本|文本|
|关闭连接|CLOSE|-|-|
|唤醒窗口|NOTIFY|-|-|
|关闭程序|SHUTDOWN|-|-|
|新建任务|TASK_NEW|B编码Map|SUCCESS/失败原因|
|任务列表|TASK_LIST|-|B编码List|
|开始任务|TASK_START|任务ID|SUCCESS/失败原因|
|暂停任务|TASK_PAUSE|任务ID|SUCCESS/失败原因|
|删除任务|TASK_DELETE|任务ID|SUCCESS/失败原因|

###### 新建任务请求主体

|名称|必要|描述|
|:--|:--|:--|
|url|√|下载链接|
|files|○|种子文件选择列表|

###### 任务列表响应主体

|名称|必要|描述|
|:--|:--|:--|
|id|√|任务ID|
|createDate|√|创建时间|
|modifyDate|√|修改时间|
|name|√|任务名称|
|type|√|任务协议|
|fileType|√|文件类型|
|file|√|文件路径或目录路径|
|url|√|下载链接|
|torrent|○|种子文件路径|
|status|√|任务状态|
|size|√|文件大小|
|endDate|○|完成时间|
|description|○|下载描述|

*√=必要、○-可选*

### 系统通知

接入扩展GUI后，可以收到系统通知。

|名称|类型|主体|
|:--|:--|:--|
|显示窗口|SHOW|-|
|隐藏窗口|HIDE|-|
|提示窗口|ALERT|B编码Map|
|提示消息|NOTICE|B编码Map|
|刷新任务|REFRESH|-|
|响应消息|RESPONSE|文本|

###### 提示窗口和提示消息主体

|名称|必要|描述|
|:--|:--|:--|
|type|√|类型|
|title|√|标题|
|message|√|内容|

### 后台模式

后台模式运行时，不使用本地GUI界面，可以通过[系统消息](#系统消息)和[系统通知](#系统通知)来完成系统管理和任务管理。

### 启动参数

|参数|默认|描述|
|:--:|:--|:--|
|`mode`|`native`|`native`：本地GUI；`extend`：扩展GUI（后台模式）；|

###### 示例

```bash
java -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar mode=[native|extend]
```

## 常见问题

### 内存溢出

使用FTP、HTTP下载大文件时如果出现内存溢出，建议优化JVM参数：`-XX:NewRatio=2 -XX:SurvivorRatio=2`

```bash
# Linux
java -server -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -jar snail.javafx-{version}.jar

# Windows
javaw -server -Xms128m -Xmx256m -XX:NewRatio=2 -XX:SurvivorRatio=2 -jar snail.javafx-{version}.jar
```

或者调小磁盘缓存

### 下载一段时间没有速度

如果出现开始下载有速度，下载一段时间后没有速度了，可以先暂停任务或重启软件再开始下载。

### 启动后没有出现GUI界面

可以查看端口`16888`和`18888`是否被占用了，查看命令：

```bash
# Linux
# 查询端口
netstat -anp|grep 16888
netstat -anp|grep 18888
# 查询占用程序
ps aux|grep pid

#Windows
# 查询端口
netstat -ano|findstr 16888
netstat -ano|findstr 18888
# 查询占用程序
tasklist|findstr pid
```

> pid：查询进程ID