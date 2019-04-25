<h1 align="center">Snail（蜗牛）</h1>

<p align="center">
基于JAVA/JAVAFX的下载工具，支持下载协议：BT、FTP、HTTP、ED2K。
</p>

<p align="center">
	<a>
		<img alt="Build" src="https://img.shields.io/badge/Build-passing-success.svg?style=flat-square" />
	</a>
	<a>
		<img alt="Version" src="https://img.shields.io/badge/Version-1.0.0-blue.svg?style=flat-square" />
	</a>
	<a href="_blank" href="https://www.acgist.com">
		<img alt="Author" src="https://img.shields.io/badge/Author-acgist-red.svg?style=flat-square" />
	</a>
	<a href="_blank" href="https://openjdk.java.net/">
		<img alt="Java" src="https://img.shields.io/badge/Java-11-yellow.svg?style=flat-square" />
	</a>
	<a href="_blank" href="https://openjfx.io/">
		<img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-12-green.svg?style=flat-square" />
	</a>
	<a href="_blank" href="https://www.bittorrent.org/beps/bep_0000.html">
		<img alt="BitTorrent" src="https://img.shields.io/badge/BitTorrent-BEP-orange.svg?style=flat-square" />
	</a>
	<a href="_blank" href="https://gitee.com/acgist/snail/releases/v1.0.0">
		<img alt="Release" src="https://img.shields.io/badge/Release-1.0.0-blueviolet.svg?style=flat-square" />
	</a>
</p>

----

## 进度
|功能|进度|优先级|
|:-|:-|:-|
|BT|○|3|
|FTP|√|2|
|HTTP|√|1|
|ED2K|×|4|

#### BT进度
|协议|进度|优先级|
|:-|:-|:-|
|DHT Protocol|×|5|
|Peer Exchange（PEX）|×|4|
|Local Service Discovery|×|7|
|Peer wire protocol（TCP）|√|2|
|Tracker Protocol（UDP/HTTP）|√|1|
|uTorrent transport protocol（uTP）|×|6|
|Extension for Peers to Send Metadata Files|√|3|

#### ED2K进度

*√=完成、○-进行中、×-未开始*

## 使用

#### 构建
```bash
# Windows构建
./builder/build.bat

# Linux构建
-
```

> lib：第三方库   
> java：java运行环境

#### Java启动
```
javaw -server -Xms128m -Xmx128m -jar snail-{version}.jar
```

#### 启动器启动

Windows直接点击SnailLauncher.exe即可运行。

> 执行程序和jar、lib、java必须处于同一个目录   
> SnailLauncher.min.exe需要系统自带MFC动态链接库
