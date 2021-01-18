# 开发帮助

## 目录

* [协议](#协议)
	* [注册协议](#注册协议)
	* [添加协议](#添加协议)
* [任务管理](#任务管理)
	* [添加任务](#添加任务)
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

## 协议

### 注册协议

```java
ProtocolManager.getInstance().register(protocol);
```

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

## 任务管理

### 添加任务

#### Snail

```java
ITaskSession taskSession = Snail.getInstance().download(url)
```

#### TaskManager

```java
ITaskSession taskSession = TaskManager.getInstance().download(url)
```

> 任务添加完成自动开始下载不用调用开始任务方法

### 开始任务

```java
ITaskSession#.start();
```

### 暂停任务

```java
ITaskSession#.pause();
```

### 删除任务

```java
ITaskSession#.delete();
```

## BT管理

### DHT管理

`NodeManager`

### Peer管理

`PeerManager`

### Tracker管理

`TrackerManager`

### Torrent管理

`TorrentManager`

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
|files|○|种子文件选择列表|

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

*√=必要、○-可选*

### 系统通知

接入扩展GUI后，可以收到系统通知。

|名称|类型|主体|
|:--|:--|:--|
|显示窗口|SHOW|-|
|隐藏窗口|HIDE|-|
|窗口消息|ALERT|[窗口消息和提示消息主体](#窗口消息和提示消息主体)|
|提示消息|NOTICE|[窗口消息和提示消息主体](#窗口消息和提示消息主体)|
|刷新任务|REFRESH|-|
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