# 开发帮助

## 目录

* [协议](#协议)
 * [添加协议](#添加协议)
 * [注册协议](#注册协议)
* [任务管理](#任务管理)
 * [添加任务](#添加任务)
 * [添加BT任务](#添加bt任务)
 * [任务信息](#任务信息)
 * [开始任务](#开始任务)
 * [暂停任务](#暂停任务)
 * [删除任务](#删除任务)
* [BT管理](#bt管理)
 * [DHT管理](#dht管理)
 * [Peer管理](#peer管理)
 * [Tracker管理](#tracker管理)
 * [Torrent管理](#torrent管理)
* [消息通知](#消息通知)
 * [系统消息](#系统消息)
 * [系统通知](#系统通知)
* [启动模式](#启动模式)
 * [后台模式](#后台模式)
 * [启动参数](#启动参数)
* [GUI事件](#gui事件)

## 协议

### 添加协议

#### 实现协议

`com.acgist.snail.protocol.Protocol`

#### 实现下载

`com.acgist.snail.downloader.Downloader`

##### 单文件下载器

`com.acgist.snail.downloader.SingleFileDownloader`

##### 多文件下载器

`com.acgist.snail.downloader.MultifileDownloader`

> 多文件下载完成调用`unlockDownload`方法结束下载

### 注册协议

```java
ProtocolContext.getInstance().register(protocol);
```

## 任务管理

### 添加任务

```java
final Snail snail = SnailBuilder.newBuilder()
// 启用FTP下载协议
//	.enableFtp()
// 启用HLS下载协议
//	.enableHls()
// 启用HTTP下载协议
//	.enableHttp()
// 启用磁力链接下载协议
//	.enableMagnet()
// 启用BT下载协议
//	.enableTorrent()
// 启用所有下载协议
	.enableAllProtocol()
// 加载下载任务
//	.loadTask()
// 启动系统监听
//	.application()
// 同步创建
	.buildSync();
// 添加下载
snail.download("下载链接");
// 等待下载完成：可以自行实现阻塞替换
snail.lockDownload();
```

> 任务添加完成自动开始下载不用调用开始任务方法

### 添加BT任务

#### 下载所有文件

```java
final String torrentPath = "种子文件";
final var snail = SnailBuilder.newBuilder()
	.enableTorrent()
	.buildSync();
// 注册文件选择事件
GuiContext.register(new MultifileEventAdapter());
// 开始下载
snail.download(torrentPath);
snail.lockDownload();
```

#### 下载指定文件

```java
final String torrentPath = "种子文件";
final var snail = SnailBuilder.newBuilder()
	.enableTorrent()
	.buildSync();
// 解析种子文件
final var torrent = TorrentContext.loadTorrent(torrentPath);
// 过滤下载文件
final var list = torrent.getInfo().files().stream()
	.filter(TorrentFile::notPaddingFile)
	.map(TorrentFile::path)
	.filter(path -> path.endsWith(".mkv"))
	.collect(Collectors.toList());
// 设置下载文件
GuiContext.getInstance().files(MultifileSelectorWrapper.newEncoder(list).serialize());
// 注册文件选择事件
GuiContext.register(new MultifileEventAdapter());
// 开始下载
snail.download(torrentPath);
snail.lockDownload();
```

#### BT任务指定优先下载位置

```java
TorrentSession.piecePos(int)
```

> 如果能够从指定位置开始选择Piece，则优先从指定位置开始下载，反之则会忽略指定位置从0开始下载。

### 任务信息

```java
// 下载状态或者下载速度（下载中）
ITaskSession.getStatusValue();
```

### 开始任务

```java
ITaskSession.start();
```

### 暂停任务

```java
ITaskSession.pause();
```

### 删除任务

```java
ITaskSession.delete();
```

## BT管理

### DHT管理

`NodeContext`

### Peer管理

`PeerContext`

### Tracker管理

`TrackerContext`

### Torrent管理

`TorrentContext`

## 消息通知

系统消息和系统通知使用B编码Map类型消息，每条消息含有类型`type`和主体`body`。

```
d4:type4:TEXT4:body7:messagee
```

> 通过`socket`连接系统端口`16888`发送系统消息和接收系统通知

> 参考实现（测试代码）：`com.acgist.snail.gui.event`

### 系统消息

通过系统消息可以实现系统管理和任务管理

|名称|类型|请求主体|响应主体|
|:--|:--|:--|:--|
|GUI注册|GUI|-|SUCCESS/失败原因|
|文本消息|TEXT|文本|文本|
|关闭连接|CLOSE|-|-|
|唤醒窗口|NOTIFY|-|-|
|关闭程序|SHUTDOWN|-|-|
|新建任务|TASK_NEW|[新建任务请求主体](#新建任务请求主体)|SUCCESS/失败原因|
|任务列表|TASK_LIST|-|[任务列表响应主体](#任务列表响应主体)|
|开始任务|TASK_START|任务ID|SUCCESS/失败原因|
|暂停任务|TASK_PAUSE|任务ID|SUCCESS/失败原因|
|删除任务|TASK_DELETE|任务ID|SUCCESS/失败原因|

#### 新建任务请求主体

B编码Map

|名称|必要|描述|
|:--|:--|:--|
|url|√|下载链接|
|files|○|选择下载文件列表|

#### 任务列表响应主体

B编码List&lt;Map&gt;

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
|payload|○|任务负载|
|statusValue|√|下载速度|

*√=必要、○-可选*

> 下载速度：下载中任务才返回速度，其他情况返回任务状态。

### 系统通知

接入扩展GUI后，可以收到系统通知。

|名称|类型|主体|
|:--|:--|:--|
|显示窗口|SHOW|-|
|隐藏窗口|HIDE|-|
|窗口消息|ALERT|[窗口消息和提示消息主体](#窗口消息和提示消息主体)|
|提示消息|NOTICE|[窗口消息和提示消息主体](#窗口消息和提示消息主体)|
|刷新任务列表|REFRESH_TASK_LIST|-|
|刷新任务状态|REFRESH_TASK_STATUS|-|
|响应消息|RESPONSE|文本|

#### 窗口消息和提示消息主体

B编码Map

|名称|必要|描述|
|:--|:--|:--|
|type|√|类型|
|title|√|标题|
|message|√|内容|

## 启动模式

### 后台模式

后台模式运行时，不使用本地GUI界面，可以通过[系统消息](#系统消息)和[系统通知](#系统通知)来完成系统管理和任务管理。

### 启动参数

|参数|默认|描述|
|:--:|:--|:--|
|`mode`|`native`|`native`：本地GUI；`extend`：扩展GUI（后台模式）；|

#### 示例

```bash
java -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar mode=[native|extend]
```

## GUI事件

GUI分为**本地GUI**和**扩展GUI**，GUI事件用来通知界面应该做出什么提示。

后台模式直接使用`GuiContext.registerAdapter()`，本地GUI按需适配。

#### GUI适配

|名称|类型|系统通知|详细描述|适配器|
|:--|:--|:--|:--|:--|
|显示窗口|SHOW|SHOW|显示窗口|ShowEventAdapter|
|隐藏窗口|HIDE|HIDE|隐藏窗口|HideEventAdapter|
|退出窗口|EXIT|-|退出系统（静默处理）|ExitEventAdapter|
|创建窗口|BUILD|-|阻塞系统（静默处理）|BuildEventAdapter|
|窗口消息|ALERT|ALERT|窗口消息|AlertEventAdapter|
|提示消息|NOTICE|NOTICE|提示消息|NoticeEventAdapter|
|响应消息|RESPONSE|RESPONSE|操作响应消息|ResponseEventAdapter|
|选择下载文件|MULTIFILE|-|选择下载文件（静默处理）|MultifileEventAdapter|
|刷新任务列表|REFRESH_TASK_LIST|REFRESH_TASK_LIST|添加任务、删除任务|RefreshTaskListEventAdapter|
|刷新任务状态|REFRESH_TASK_STATUS|REFRESH_TASK_STATUS|开始任务、暂停任务|RefreshTaskStatusEventAdapter|