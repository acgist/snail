<h1 align="center">Snail（蜗牛）</h1>

<p align="center">
基于Java、JavaFX开发的下载工具，支持下载协议：BT（BitTorrent）、FTP、HTTP。
</p>

<p align="center">
	<a target="_blank" href="https://www.acgist.com">
		<img alt="Author" src="https://img.shields.io/badge/Author-acgist-red.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://gitee.com/acgist/snail">
		<img alt="Version" src="https://img.shields.io/badge/Version-1.2.2-lightgrey.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://gitee.com/acgist/snail/releases/v1.2.1">
		<img alt="Release" src="https://img.shields.io/badge/Release-1.2.1-blueviolet.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://openjdk.java.net/">
		<img alt="Java" src="https://img.shields.io/badge/Java-11-yellow.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://openjfx.io/">
		<img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-11-blue.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://www.bittorrent.org/beps/bep_0000.html">
		<img alt="BitTorrent" src="https://img.shields.io/badge/BitTorrent-BEP-orange.svg?style=flat-square" />
	</a>
	<br />
	<img alt="Travis (.org)" src="https://img.shields.io/travis/acgist/snail?style=flat-square" />
	<img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/acgist/snail?color=crimson&style=flat-square" />
	<img alt="GitHub" src="https://img.shields.io/github/license/acgist/snail?style=flat-square" />
</p>

----

## 使用

Windows稳定版下载：[snail-windows-v1.2.1.zip](https://gitee.com/acgist/snail/attach_files)（解压可以直接运行）

#### 编译

```bash
# 编译系统和运行系统不一致时添加参数：-D javafx.platform=win|mac|linux
mvn clean package -P release -D skipTests
```

> 推荐下载[发行版](https://gitee.com/acgist/snail/releases)编译（最新分支可能存在未开发完成的任务）

#### 启动

```bash
# Linux
java -server -Xms128m -Xmx256m -jar snail-{version}.jar

# Windows
javaw -server -Xms128m -Xmx256m -jar snail-{version}.jar
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

|协议|进度|
|:--|:--:|
|最终||
|[The BitTorrent Protocol Specification](http://www.bittorrent.org/beps/bep_0003.html)|√|
|[Known Number Allocations](http://www.bittorrent.org/beps/bep_0004.html)|√|
|[Peer ID Conventions](http://www.bittorrent.org/beps/bep_0020.html)|√|
|接受||
|[DHT Protocol](http://www.bittorrent.org/beps/bep_0005.html)|√|
|[Fast Extension](http://www.bittorrent.org/beps/bep_0006.html)|√|
|[Extension for Peers to Send Metadata Files](http://www.bittorrent.org/beps/bep_0009.html)|√|
|[Extension Protocol](http://www.bittorrent.org/beps/bep_0010.html)|√|
|[Peer Exchange (PEX)](http://www.bittorrent.org/beps/bep_0011.html)|√|
|[Multitracker Metadata Extension](http://www.bittorrent.org/beps/bep_0012.html)|√|
|[Local Service Discovery](http://www.bittorrent.org/beps/bep_0014.html)|√|
|[UDP Tracker Protocol for BitTorrent](http://www.bittorrent.org/beps/bep_0015.html)|√|
|[HTTP/FTP Seeding (GetRight-style)](http://www.bittorrent.org/beps/bep_0019.html)|?|
|[Tracker Returns Compact Peer Lists](http://www.bittorrent.org/beps/bep_0023.html)|√|
|[Private Torrents](http://www.bittorrent.org/beps/bep_0027.html)|√|
|[uTorrent Transport Protocol](http://www.bittorrent.org/beps/bep_0029.html)|√|
|[Holepunch Extension](http://www.bittorrent.org/beps/bep_0055.html)|√|
|草案||
|[IPv6 Tracker Extension](http://www.bittorrent.org/beps/bep_0007.html)|×|
|[Superseeding](http://www.bittorrent.org/beps/bep_0016.html)|?|
|[HTTP Seeding (Hoffman-style)](http://www.bittorrent.org/beps/bep_0017.html)|?|
|[Extension for partial seeds](http://www.bittorrent.org/beps/bep_0021.html)|○|
|[Merkle tree torrent extension](http://www.bittorrent.org/beps/bep_0030.html)|?|
|[Failure Retry Extension](http://www.bittorrent.org/beps/bep_0031.html)|?|
|[IPv6 extension for DHT](http://www.bittorrent.org/beps/bep_0032.html)|×|
|[DHT scrape](http://www.bittorrent.org/beps/bep_0033.html)|?|
|[Torrent Signing](http://www.bittorrent.org/beps/bep_0035.html)|?|
|[UDP Tracker Protocol Extensions](http://www.bittorrent.org/beps/bep_0041.html)|?|
|[DHT Security Extension](http://www.bittorrent.org/beps/bep_0042.html)|?|
|[Read-only DHT Nodes](http://www.bittorrent.org/beps/bep_0043.html)|?|
|[Storing arbitrary data in the DHT](http://www.bittorrent.org/beps/bep_0044.html)|?|
|[Multiple-address operation for the BitTorrent DHT](http://www.bittorrent.org/beps/bep_0045.html)|?|
|[Updating Torrents Via DHT Mutable Items](http://www.bittorrent.org/beps/bep_0046.html)|?|
|[Padding files and extended file attributes](http://www.bittorrent.org/beps/bep_0047.html)|?|
|[Tracker Protocol Extension: Scrape](http://www.bittorrent.org/beps/bep_0048.html)|○|
|[Publish/Subscribe Protocol](http://www.bittorrent.org/beps/bep_0050.html)|?|
|[DHT Infohash Indexing](http://www.bittorrent.org/beps/bep_0051.html)|?|
|[The BitTorrent Protocol Specification v2](http://www.bittorrent.org/beps/bep_0052.html)|?|
|[Magnet URI extension - Select specific file indices for download](http://www.bittorrent.org/beps/bep_0053.html)|?|
|[The lt_donthave extension](http://www.bittorrent.org/beps/bep_0054.html)|√|
|其他||
|IPv6|○|
|[STUN](https://www.rfc-editor.org/rfc/rfc5389.txt)|√|
|[UPnP](http://upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.0.pdf)|√|
|upload_only|√|
|[Message Stream Encryption](https://wiki.vuze.com/w/Message_Stream_Encryption)|√|

*√=完成、○-进行中、×-未开始、?-待定*

## License

|软件|License|许可|
|:--|:--:|:--|
|[h2](http://www.h2database.com)|[License](http://www.h2database.com/html/license.html)|MPL 2.0/EPL 1.0|
|[slf4j](https://www.slf4j.org)|[License](https://www.slf4j.org/license.html)|MIT|
|[logback](https://logback.qos.ch)|[License](https://logback.qos.ch/license.html)|LGPL 2.1/EPL 1.0|
|[JavaFX](https://wiki.openjdk.java.net/display/OpenJFX/Main)|[License](http://openjdk.java.net/legal/gplv2+ce.html)|GPL 2.0|
|[Snail](https://gitee.com/acgist/snail)|[License](https://gitee.com/acgist/snail/blob/master/LICENSE)|BSD 3-clause|

## 其他

#### GUI开发

JavaFX Scene Builder

#### GIT仓库

GITEE：[https://gitee.com/acgist/snail](https://gitee.com/acgist/snail)  
GITHUB：[https://github.com/acgist/snail](https://github.com/acgist/snail)

#### 维基

[Wiki](https://gitee.com/acgist/snail/wikis)

#### 界面

![界面](https://static.acgist.com/demo/snail/snail.png "界面")

#### 更多

[https://www.acgist.com/snail](https://www.acgist.com/snail)

## 贡献

欢迎大家提出问题和建议，但是请不要提交到评论区（会被删除），如果有问题和建议请提交[Issues](https://gitee.com/acgist/snail/issues)

提交Issues/PR前请阅读[贡献规范](./CONTRIBUTING.md)

提交PR前请阅读[代码规范](./CODE_OF_CONDUCT.md)

谢谢！