<h1 align="center">Snail（蜗牛）</h1>

<p align="center">
基于Java、JavaFX开发的下载工具，支持下载协议：BT（BitTorrent、磁力链接、种子）、FTP、HTTP。
</p>

<p align="center">
	<!--
	<a target="_blank" href="https://www.acgist.com">
		<img alt="Author" src="https://img.shields.io/badge/Author-acgist-red.svg?style=flat-square" />
	</a>
	-->
	<a target="_blank" href="https://openjdk.java.net/">
		<img alt="Java" src="https://img.shields.io/badge/Java-11-yellow.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://openjfx.io/">
		<img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-11-blueviolet.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://www.bittorrent.org/beps/bep_0000.html">
		<img alt="BitTorrent" src="https://img.shields.io/badge/BitTorrent-BEP-orange.svg?style=flat-square" />
	</a>
	<br />
	<img alt="Travis (.org)" src="https://img.shields.io/travis/acgist/snail?style=flat-square" />
	<img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/acgist/snail?style=flat-square" />
	<img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/acgist/snail?color=crimson&style=flat-square" />
	<img alt="GitHub" src="https://img.shields.io/github/license/acgist/snail?style=flat-square" />
</p>

----

## 结构

|项目|描述|
|:--|:--|
|snail|下载核心|
|snail-javafx|电脑GUI|
|snail-extend|扩展GUI|
|snail-android|安卓GUI|

## 使用

Windows稳定版下载：[snail-windows.zip](https://gitee.com/acgist/snail/attach_files)（解压可以直接运行）

#### 编译

```bash
# 编译系统和运行系统不一致时添加编译参数：-D javafx.platform=win|mac|linux
mvn clean package -P release -D skipTests
```

> 推荐下载[发行版](https://gitee.com/acgist/snail/releases)编译（最新分支可能存在未开发完成的任务）

#### 启动

下载核心无法直接启动，需要结合`snail.javafx`或者`snail.extend`使用。

```bash
# Linux
java -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar

# Windows
javaw -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar
```

#### Maven

```xml
<dependency>
	<groupId>com.acgist</groupId>
	<artifactId>snail</artifactId>
	<version>{release.version}</version>
</dependency>
```

## 开发进度

|功能|进度|
|:--|:--:|
|BT|○|
|FTP|√|
|HTTP|√|

#### 协议进度

|编号|协议|进度|
|:--:|:--|:--:|
|最终|-|-|
|0003|[The BitTorrent Protocol Specification](http://www.bittorrent.org/beps/bep_0003.html)|√|
|0004|[Known Number Allocations](http://www.bittorrent.org/beps/bep_0004.html)|√|
|0020|[Peer ID Conventions](http://www.bittorrent.org/beps/bep_0020.html)|√|
|接受|-|-|
|0005|[DHT Protocol](http://www.bittorrent.org/beps/bep_0005.html)|√|
|0006|[Fast Extension](http://www.bittorrent.org/beps/bep_0006.html)|√|
|0009|[Extension for Peers to Send Metadata Files](http://www.bittorrent.org/beps/bep_0009.html)|√|
|0010|[Extension Protocol](http://www.bittorrent.org/beps/bep_0010.html)|√|
|0011|[Peer Exchange (PEX)](http://www.bittorrent.org/beps/bep_0011.html)|√|
|0012|[Multitracker Metadata Extension](http://www.bittorrent.org/beps/bep_0012.html)|√|
|0014|[Local Service Discovery](http://www.bittorrent.org/beps/bep_0014.html)|√|
|0015|[UDP Tracker Protocol](http://www.bittorrent.org/beps/bep_0015.html)|√|
|0019|[HTTP/FTP Seeding (GetRight-style)](http://www.bittorrent.org/beps/bep_0019.html)|?|
|0023|[Tracker Returns Compact Peer Lists](http://www.bittorrent.org/beps/bep_0023.html)|√|
|0027|[Private Torrents](http://www.bittorrent.org/beps/bep_0027.html)|√|
|0029|[uTorrent Transport Protocol](http://www.bittorrent.org/beps/bep_0029.html)|√|
|0055|[Holepunch Extension](http://www.bittorrent.org/beps/bep_0055.html)|√|
|草案|-|-|
|0007|[IPv6 Tracker Extension](http://www.bittorrent.org/beps/bep_0007.html)|×|
|0016|[Superseeding](http://www.bittorrent.org/beps/bep_0016.html)|?|
|0017|[HTTP Seeding (Hoffman-style)](http://www.bittorrent.org/beps/bep_0017.html)|?|
|0021|[Extension for partial seeds](http://www.bittorrent.org/beps/bep_0021.html)|○|
|0030|[Merkle tree torrent extension](http://www.bittorrent.org/beps/bep_0030.html)|?|
|0031|[Tracker Failure Retry Extension](http://www.bittorrent.org/beps/bep_0031.html)|?|
|0032|[IPv6 extension for DHT](http://www.bittorrent.org/beps/bep_0032.html)|×|
|0033|[DHT scrape](http://www.bittorrent.org/beps/bep_0033.html)|?|
|0035|[Torrent Signing](http://www.bittorrent.org/beps/bep_0035.html)|?|
|0041|[UDP Tracker Protocol Extensions](http://www.bittorrent.org/beps/bep_0041.html)|?|
|0042|[DHT Security Extension](http://www.bittorrent.org/beps/bep_0042.html)|?|
|0043|[Read-only DHT Nodes](http://www.bittorrent.org/beps/bep_0043.html)|?|
|0044|[Storing arbitrary data in the DHT](http://www.bittorrent.org/beps/bep_0044.html)|?|
|0045|[Multiple-address operation for the BitTorrent DHT](http://www.bittorrent.org/beps/bep_0045.html)|?|
|0046|[Updating Torrents Via DHT Mutable Items](http://www.bittorrent.org/beps/bep_0046.html)|?|
|0047|[Padding files and extended file attributes](http://www.bittorrent.org/beps/bep_0047.html)|?|
|0048|[Tracker Protocol Extension: Scrape](http://www.bittorrent.org/beps/bep_0048.html)|○|
|0050|[Publish/Subscribe Protocol](http://www.bittorrent.org/beps/bep_0050.html)|?|
|0051|[DHT Infohash Indexing](http://www.bittorrent.org/beps/bep_0051.html)|?|
|0052|[The BitTorrent Protocol Specification v2](http://www.bittorrent.org/beps/bep_0052.html)|?|
|0053|[Magnet URI extension - Select specific file indices for download](http://www.bittorrent.org/beps/bep_0053.html)|?|
|0054|[The lt_donthave extension](http://www.bittorrent.org/beps/bep_0054.html)|√|
|其他|-|-|
|-|IPv6|○|
|-|[STUN](https://www.rfc-editor.org/rfc/rfc5389.txt)|√|
|-|[UPnP](http://upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.0.pdf)|√|
|-|upload_only|√|
|-|[Message Stream Encryption](https://wiki.vuze.com/w/Message_Stream_Encryption)|√|

*√=完成、○-进行中、×-未开始、?-待定*

## 依赖项目

|软件|License|许可|
|:--|:--:|:--|
|[h2](http://www.h2database.com)|[License](http://www.h2database.com/html/license.html)|MPL 2.0/EPL 1.0|
|[slf4j](https://www.slf4j.org)|[License](https://www.slf4j.org/license.html)|MIT|
|[logback](https://logback.qos.ch)|[License](https://logback.qos.ch/license.html)|LGPL 2.1/EPL 1.0|
|[JavaFX](https://wiki.openjdk.java.net/display/OpenJFX/Main)|[License](http://openjdk.java.net/legal/gplv2+ce.html)|GPL 2.0|

## 视频播放

蜗牛专注下载，不会实现边下边播功能。

#### 技巧

虽然没有实现边下边播功能，但是理论上下载是按序下载，所以已下载的部分可以直接使用播放器进行播放。

> 可以通过`F12`查看已经下载的部分

## 其他

#### 维基

[Wiki](https://gitee.com/acgist/snail/wikis)

#### GIT

GITEE：[https://gitee.com/acgist/snail](https://gitee.com/acgist/snail)

GITHUB：[https://github.com/acgist/snail](https://github.com/acgist/snail)

#### GUI

![蜗牛](https://static.acgist.com/demo/snail/snail.png "蜗牛")
![统计](https://static.acgist.com/demo/snail/statistics03.png "统计")

> GUI绘制工具：JavaFX Scene Builder

#### 更多

[https://www.acgist.com/snail](https://www.acgist.com/snail)

## 贡献

欢迎大家提出问题和建议，但是请不要提交到评论区（会被删除），如果有问题和建议请提交[Issues](https://gitee.com/acgist/snail/issues)

提交Issues/PR前请阅读[贡献规范](./CONTRIBUTING.md)

提交PR前请阅读[代码规范](./CODE_OF_CONDUCT.md)

谢谢！