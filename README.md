<h1 align="center">Snail（蜗牛）</h1>

<p align="center">
基于Java、JavaFX开发的下载工具，支持下载协议：BT（BitTorrent、磁力链接、种子文件）、HLS（M3U8）、FTP、HTTP。
</p>

<p align="center">
	<a target="_blank" href="https://openjdk.java.net">
		<img alt="Java" src="https://img.shields.io/badge/Java-11-yellow.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://openjfx.io">
		<img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-11-blueviolet.svg?style=flat-square" />
	</a>
	<a target="_blank" href="https://github.com/acgist/snail">
		<img alt="GitHub stars" src="https://img.shields.io/github/stars/acgist/snail?color=red&style=flat-square" />
	</a>
	<a target="_blank" href="https://gitee.com/acgist/snail">
		<img alt="Gitee stars" src="https://gitee.com/acgist/snail/badge/star.svg?theme=dark" />
	</a>
	<br />
	<img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/acgist/snail/build?style=flat-square">
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

## 使用

稳定版本下载：[snail-windows.zip](https://gitee.com/acgist/snail/attach_files)（解压可以直接运行）

> 稳定版本提供`mac`、`linux`、`windows`三个版本

#### 编译

```bash
# 编译系统和运行系统不一致时添加编译参数：-D javafx.platform=win|mac|linux
mvn clean package -P release -D skipTests
```

> 推荐下载[发行版](https://gitee.com/acgist/snail/releases)编译

#### Gui

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

```java
final Snail snail = SnailBuilder.getInstance()
	.enableAllProtocol()
	.buildSync();
snail.download("https://www.acgist.com");
snail.lockDownload();
```

[更多帮助](./docs/HELP.md)

## 开发进度

|功能|进度|
|:--|:--:|
|BT|√|
|FTP|√|
|HLS|√|
|HTTP|√|

*√=完成、○-进行中、×-未开始、?-待定*

> [协议进度](./docs/PROTOCOL.md)

## 依赖项目

|软件|许可|
|:--|:--|
|[slf4j](https://www.slf4j.org)|[MIT](https://www.slf4j.org/license.html)|
|[JavaFX](https://wiki.openjdk.java.net/display/OpenJFX)|[GPLv2 + CE](https://openjdk.java.net/legal/gplv2+ce.html)|
|[OpenJDK](https://openjdk.java.net)|[GPLv2 + CE](https://openjdk.java.net/legal/gplv2+ce.html)|

## 视频播放

蜗牛专注下载，不会实现边下边播功能。

#### 技巧

虽然没有实现边下边播功能，但是理论上下载是按序下载，所以已下载的部分可以直接使用播放器进行播放。

> 可以通过`F12`查看已下载的部分

## 其他

#### GIT

GITEE：[https://gitee.com/acgist/snail](https://gitee.com/acgist/snail)

GITHUB：[https://github.com/acgist/snail](https://github.com/acgist/snail)

#### GUI

![蜗牛](https://static.acgist.com/demo/snail/snail.png "蜗牛")
![统计](https://static.acgist.com/demo/snail/statistics03.png "统计")

#### 更多

[https://www.acgist.com/snail](https://www.acgist.com/snail)

## 贡献

提交PR前请阅读[代码规范](./CODE_OF_CONDUCT.md)、[贡献规范](./CONTRIBUTING.md)

问题和建议请提交到[Issues](https://gitee.com/acgist/snail/issues)，提交前请阅读[贡献规范](./CONTRIBUTING.md)。

谢谢！