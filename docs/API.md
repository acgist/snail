# API

## 协议

#### 注册协议

```java
ProtocolManager.getInstance().register(protocol);
```

#### 添加协议

继承`com.acgist.snail.protocol.Protocol`

## 任务管理

#### 添加任务

###### Snail

```java
Snail.getInstance().download(url)
```

###### DownloaderManager

```java
DownloaderManager.getInstance().download(url)
```

#### 暂停任务

###### Snail

```java
Snail.getInstance().pause(url)
```

###### DownloaderManager

```java
DownloaderManager.getInstance().pause(url)
```

#### 删除任务

###### Snail

```java
Snail.getInstance().delete(url)
```

###### DownloaderManager

```java
DownloaderManager.getInstance().delete(url)
```

## BT管理

#### DHT管理

`NodeManager`

#### Peer管理

`PeerManager`

#### Tracker管理

`TrackerManager`

#### Torrent管理

`TorrentManager`