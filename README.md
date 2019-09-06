<h1 align="center">Snail（蜗牛）</h1>

<p align="center">
基于Java/JavaFX的下载工具，支持下载协议：BT（BitTorrent）、FTP、HTTP。
</p>

<p align="center">
	<a target="_blank" href="https://www.acgist.com">
		<img alt="Author" src="https://img.shields.io/badge/Author-acgist-red.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://gitee.com/acgist/snail">
		<img alt="Version" src="https://img.shields.io/badge/Version-1.1.1-blue.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://gitee.com/acgist/snail/releases/v1.1.0">
		<img alt="Release" src="https://img.shields.io/badge/Release-1.1.0-blueviolet.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://openjdk.java.net/">
		<img alt="Java" src="https://img.shields.io/badge/Java-11-yellow.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://openjfx.io/">
		<img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-11-green.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://www.bittorrent.org/beps/bep_0000.html">
		<img alt="BitTorrent" src="https://img.shields.io/badge/BitTorrent-BEP-orange.svg?style=flat-square" />
	</a>
</p>

----

## 使用

#### 编译

```bash
# 编译系统和运行系统不一致时，请修改pom.xml -> os.name属性=目标系统并修改JavaFX依赖，编译完成后可以删除lib目录中编译系统的JavaFX依赖。

# Windows
./builder/build.bat

# Linux
mvn clean package -P release -D skipTests
```

> lib：依赖  
> java：Java运行环境

#### 命令行启动

```bash
# Windows
javaw -server -Xms256m -Xmx256m -jar snail-{version}.jar

# Linux
java -server -Xms256m -Xmx256m -jar snail-{version}.jar
```

#### 启动器启动

Windows直接点击SnailLauncher.exe即可运行。

> 执行程序和jar、lib、java必须处于同一个目录

## 进度

|功能|进度|
|:-|:-|
|BT|○|
|FTP|√|
|HTTP|√|

#### BT进度

|协议（BEP）|进度|
|:-|:-|
|[DHT Protocol](http://www.bittorrent.org/beps/bep_0005.html)|√|
|[Fast Extension](http://www.bittorrent.org/beps/bep_0006.html)|×|
|[Private Torrents](http://www.bittorrent.org/beps/bep_0027.html)|○|
|[Extension Protocol](http://www.bittorrent.org/beps/bep_0010.html)|√|
|[Holepunch Extension](http://www.bittorrent.org/beps/bep_0055.html)|○|
|[Peer Exchange (PEX)](http://www.bittorrent.org/beps/bep_0011.html)|√|
|[Peer ID Conventions](http://www.bittorrent.org/beps/bep_0020.html)|√|
|[Local Service Discovery](http://www.bittorrent.org/beps/bep_0014.html)|√|
|[uTorrent Transport Protocol](http://www.bittorrent.org/beps/bep_0029.html)|√|
|[Multitracker Metadata Extension](http://www.bittorrent.org/beps/bep_0012.html)|√|
|[Tracker Returns Compact Peer Lists](http://www.bittorrent.org/beps/bep_0023.html)|√|
|[UDP Tracker Protocol for BitTorrent](http://www.bittorrent.org/beps/bep_0015.html)|√|
|[The BitTorrent Protocol Specification](http://www.bittorrent.org/beps/bep_0003.html)|√|
|[Extension for Peers to Send Metadata Files](http://www.bittorrent.org/beps/bep_0009.html)|√|
|加密（PEX/Peer流量）|x|
|IPv6（(DHT/Peer/PEX/Tracker）|x|

*√=完成、○-进行中、×-未开始、?-待定*

## License

|软件|License|许可|
|:-|:-|:-|
|[h2](http://www.h2database.com)|[License](http://www.h2database.com/html/license.html)|MPL 2.0/EPL 1.0|
|[slf4j](https://www.slf4j.org)|[License](https://www.slf4j.org/license.html)|MIT|
|[logback](https://logback.qos.ch)|[License](https://logback.qos.ch/license.html)|LGPL 2.1/EPL 1.0|
|[JavaFX](https://wiki.openjdk.java.net/display/OpenJFX/Main)|[License](http://openjdk.java.net/legal/gplv2+ce.html)|GPL 2.0|
|[Snail](https://gitee.com/acgist/snail)|[License](https://gitee.com/acgist/snail/blob/master/LICENSE)|BSD 3-clause|

## 其他

###### GUI开发工具

JavaFX Scene Builder

###### GIT

GITEE：[https://gitee.com/acgist/snail](https://gitee.com/acgist/snail)  
GITHUB（不活跃）：[https://github.com/acgist/snail](https://github.com/acgist/snail)

[帮助](https://gitee.com/acgist/snail/wikis)

###### 界面
![下载界面](https://static.acgist.com/resources/images/snail.png "下载界面")