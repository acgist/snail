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
|snail-javafx|电脑界面|

## 使用

稳定版本下载：[snail-windows.zip](https://gitee.com/acgist/snail/attach_files)（解压可以直接运行）

> 稳定版本提供`mac`、`linux`、`windows`三个版本

### 编译

```bash
mvn clean package -P release -D skipTests
```

> 推荐使用[稳定版](https://gitee.com/acgist/snail/releases)编译

### Gui

```bash
# Linux
java -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar

# Windows
javaw -server -Xms128m -Xmx256m -jar snail.javafx-{version}.jar
```

### Maven

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
// 注册文件选择事件（BT任务）
// GuiContext.register(new MultifileEventAdapter());
snail.download("https://www.acgist.com");
snail.lockDownload();
```

### 帮助

[开发帮助](./docs/API.md)

[使用帮助](./docs/HELP.md)

## 依赖项目

|软件|许可|
|:--|:--|
|[slf4j](https://www.slf4j.org)|[MIT](https://www.slf4j.org/license.html)|
|[JavaFX](https://wiki.openjdk.java.net/display/OpenJFX)|[GPLv2 + CE](https://openjdk.java.net/legal/gplv2+ce.html)|
|[OpenJDK](https://openjdk.java.net)|[GPLv2 + CE](https://openjdk.java.net/legal/gplv2+ce.html)|

## 其他

### GIT

GITEE：[https://gitee.com/acgist/snail](https://gitee.com/acgist/snail)

GITHUB：[https://github.com/acgist/snail](https://github.com/acgist/snail)

### 界面

![蜗牛](./docs/gui/main.png "蜗牛")
![统计](./docs/gui/statistics-piece.png "统计")

[界面操作](./docs/GUI.md)

### 协议

[协议信息](./docs/PROTOCOL.md)

### 贡献

提交PR前请阅读[代码规范](./CODE_OF_CONDUCT.md)、[贡献规范](./CONTRIBUTING.md)

问题和建议请提交到[Issues](https://gitee.com/acgist/snail/issues)，提交前请阅读[贡献规范](./CONTRIBUTING.md)。

谢谢！

### 捐赠

[捐赠](https://www.acgist.com/sponsor)

[服务器](https://www.acgist.com/collect/server)

### 更多

[https://www.acgist.com/snail](https://www.acgist.com/snail)
