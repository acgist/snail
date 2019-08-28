<h1 align="center">Snail（蜗牛）</h1>

<p align="center">
基于Java/JavaFX的下载工具，支持下载协议：BT、FTP、HTTP。
</p>

<p align="center">
	<a target="_blank" href="https://www.acgist.com">
		<img alt="Author" src="https://img.shields.io/badge/Author-acgist-red.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://gitee.com/acgist/snail">
		<img alt="Version" src="https://img.shields.io/badge/Version-1.1.0-blue.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://gitee.com/acgist/snail/releases/v1.0.2">
		<img alt="Release" src="https://img.shields.io/badge/Release-1.0.2-blueviolet.svg?style=flat-square" />
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

#### 构建

```bash
# 构建时请修改Maven配置（pom.xml）中os.name=使用系统的名称。
# 如果构建系统和使用系统不一致时，可以删除构建后lib目录下JavaFX不需要的依赖。

# Windows
./builder/build.bat

# Linux
mvn clean package -Prelease -DskipTests
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

## 依赖

数据库：[h2](http://www.h2database.com)

日志框架：[slf4j](https://www.slf4j.org/)、[logback](https://logback.qos.ch/)

## 进度

|功能|进度|
|:-|:-|
|BT|○|
|FTP|√|
|HTTP|√|

#### BT进度

|协议|进度|
|:-|:-|
|DHT Protocol|√|
|Extension Protocol|√|
|Peer Exchange（PEX）|√|
|Peer wire Protocol（TCP）|√|
|Tracker Protocol（UDP/HTTP）|√|
|uTorrent Transport Protocol（uTP）|√|
|Extension for Peers to Send Metadata Files|√|
|Fast Extension|×|
|Holepunch Extension|×|
|Local Service Discovery|×|
|HTTP Seeding|?|
|WebSeed - HTTP/FTP Seeding|?|

#### TODO

|任务|
|:-|
|支持IPv6|
|支持加密|

*√=完成、○-进行中、×-未开始、?-待定*

## 其他

###### GUI开发工具

JavaFX Scene Builder

###### GIT

GITEE：[https://gitee.com/acgist/snail](https://gitee.com/acgist/snail)  
GITHUB（不活跃）：[https://github.com/acgist/snail](https://github.com/acgist/snail)

###### 帮助
[帮助](./HELP.md)

###### 界面
![下载界面](https://static.acgist.com/resources/images/snail.png "下载界面")