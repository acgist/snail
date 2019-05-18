<h1 align="center">Snail（蜗牛）</h1>

<p align="center">
基于Java/JavaFX的下载工具，支持下载协议：BT、FTP、HTTP、ED2K。
</p>

<p align="center">
	<a>
		<img alt="Build" src="https://img.shields.io/badge/Build-passing-success.svg?style=flat-square" />
	</a>
	<a>
		<img alt="Version" src="https://img.shields.io/badge/Version-1.0.2-blue.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://www.acgist.com">
		<img alt="Author" src="https://img.shields.io/badge/Author-acgist-red.svg?style=flat-square" />
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
	<a target="_blank" href="https://gitee.com/acgist/snail/releases/v1.0.2">
		<img alt="Release" src="https://img.shields.io/badge/Release-1.0.2-blueviolet.svg?style=flat-square" />
	</a>
</p>

----

## 进度

|功能|进度|
|:-|:-|
|BT|○|
|FTP|√|
|HTTP|√|
|ED2K|×|

#### BT进度

|协议|进度|
|:-|:-|
|DHT Protocol|√|
|Fast Extension|×|
|Extension Protocol|√|
|Peer Exchange（PEX）|√|
|Holepunch extension|×|
|Local Service Discovery|×|
|Peer wire protocol（TCP）|√|
|Tracker Protocol（UDP/HTTP）|√|
|uTorrent transport protocol（uTP）|○|
|Extension for Peers to Send Metadata Files|√|

#### ED2K进度

*√=完成、○-进行中、×-未开始*

## 使用

#### 构建

```bash
# Windows
./builder/build.bat

# Linux
mvn clean package -Prelease -DskipTests
```

> lib：依赖  
> java：Java运行环境

#### Java启动

```bash
# Windows
javaw -server -Xms256m -Xmx256m -jar snail-{version}.jar

# Linux
java -server -Xms256m -Xmx256m -jar snail-{version}.jar
```

#### 启动器启动

Windows直接点击SnailLauncher.exe即可运行。

> 执行程序和jar、lib、java必须处于同一个目录

## 下载界面
![下载界面](http://files.git.oschina.net/group1/M00/07/B8/PaAvDFzd9lCAUSXEAAB8UcH2axw194.png "下载界面")