# 开发帮助

## 下载协议

|功能|接口/超类/方法|说明|
|:-|:-|:-|
|添加协议|`Protocol`||
|注册协议|`ProtocolContext.getInstance().register(protocol);`||
|实现下载|`Downloader`||
|单文件下载器|`SingleFileDownloader`||
|多文件下载器|`MultifileDownloader`|下载完成调用`unlockDownload`方法结束下载|

## 任务管理

|功能|方法|说明|
|:--|:--|:--|
|任务状态|`ITaskSession.getStatusValue();`|下载状态或者下载速度|
|开始任务|`ITaskSession.start();`||
|暂停任务|`ITaskSession.pause();`||
|删除任务|`ITaskSession.delete();`||

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
// 启用BitTorrent下载协议
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

### 添加BT任务

```java
final String torrentPath = "种子文件";
final var snail = SnailBuilder.newBuilder()
	.enableTorrent()
	.buildSync();
// 注册文件选择事件
GuiContext.register(new MultifileEventAdapter());
// 解析种子文件
final var torrent = TorrentContext.loadTorrent(torrentPath);
// 过滤下载文件
final var list = torrent.getInfo().files().stream()
	.filter(TorrentFile::notPaddingFile)
	.map(TorrentFile::path)
	.filter(path -> path.endsWith(".mkv"))
	.collect(Collectors.toList());
// 设置下载文件
MultifileEventAdapter.files(MultifileSelectorWrapper.newEncoder(list).serialize());
// 开始下载
snail.download(torrentPath);
// 等待下载完成
snail.lockDownload();
```

#### BT协议管理

|协议|管理工具|
|:--|:--|
|DHT|`DhtContext`/`NodeContext`|
|Peer|`PeerContext`|
|Tracker|`TrackerContext`|
|Torrent|`TorrentContext`|

#### BT任务指定优先下载位置

```java
TorrentSession.piecePos(int)
```

> 默认从头开始顺序下载，指定后优先从指定位置开始顺序下载。

## 消息通知

系统消息和系统通知使用B编码`Map`类型消息，每条消息含有类型`type`和主体`body`。

```
d4:type4:TEXT4:body7:messagee
```

> 通过`socket`连接系统端口`16888`发送系统消息和接收系统通知

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

B编码`Map`

|名称|必要|描述|
|:--|:--|:--|
|url|○|下载链接或者种子路径|
|files|○|选择下载文件列表|

> 下载多文件任务时参考`MULTIFILE`通知

#### 任务列表响应主体

B编码`List<Map>`

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
|completedDate|○|完成时间|
|description|○|下载描述|
|payload|○|任务负载|
|statusValue|√|下载状态|

*√=必要、○-可选*

### 系统通知

通知系统事件，方便GUI做出相应操作。

|名称|类型|主体|
|:--|:--|:--|
|显示窗口|SHOW|-|
|隐藏窗口|HIDE|-|
|窗口消息|ALERT|[窗口消息和提示消息主体](#窗口消息和提示消息主体)|
|提示消息|NOTICE|[窗口消息和提示消息主体](#窗口消息和提示消息主体)|
|选择下载文件|MULTIFILE|[文件选择消息主体](#文件选择消息主体)|
|刷新任务列表|REFRESH_TASK_LIST|-|
|刷新任务状态|REFRESH_TASK_STATUS|-|
|响应消息|RESPONSE|文本|

#### 窗口消息和提示消息主体

B编码`Map`

|名称|必要|描述|
|:--|:--|:--|
|type|√|类型|
|title|√|标题|
|message|√|内容|

#### 文件选择消息主体

B编码`Map`

|名称|必要|描述|
|:--|:--|:--|
|path|√|路径|
|length|√|大小|

> 选择文件发送`TASK_NEW`系统消息进行下载

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

GUI分为**本地GUI**和**扩展GUI**，GUI事件用来通知界面应该做出什么响应。

后台模式直接使用`GuiContext.registerAdapter()`，本地GUI按需适配。

### GUI适配

|名称|类型|系统通知|详细描述|适配器|
|:--|:--|:--|:--|:--|
|显示窗口|SHOW|SHOW|显示窗口|ShowEventAdapter|
|隐藏窗口|HIDE|HIDE|隐藏窗口|HideEventAdapter|
|退出窗口|EXIT|-|退出系统（静默处理）|ExitEventAdapter|
|创建窗口|BUILD|-|阻塞系统（静默处理）|BuildEventAdapter|
|窗口消息|ALERT|ALERT|窗口消息|AlertEventAdapter|
|提示消息|NOTICE|NOTICE|提示消息|NoticeEventAdapter|
|选择下载文件|MULTIFILE|MULTIFILE|选择下载文件|MultifileEventAdapter|
|刷新任务列表|REFRESH_TASK_LIST|REFRESH_TASK_LIST|添加任务、删除任务|RefreshTaskListEventAdapter|
|刷新任务状态|REFRESH_TASK_STATUS|REFRESH_TASK_STATUS|开始任务、暂停任务|RefreshTaskStatusEventAdapter|
|响应消息|RESPONSE|RESPONSE|操作响应消息|ResponseEventAdapter|

## 测试数据

[测试数据](https://pan.baidu.com/s/1awl2rubJJNbdz5GBGMNx7Q)

> 提取码：`16pd`
