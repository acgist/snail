# Snail（蜗牛）

#### 介绍
基于JAVA/JAVAFX的下载工具，支持下载协议：BT、FTP、HTTP、ED2K。

#### 技术
|技术|版本|
|:-|:-|
|JAVA|11|
|JAVAFX|12|

#### 包
|包路径|作用说明|
|:-|:-|
|com.acgist.snail.downloader|下载器|
|com.acgist.snail.gui|图形界面|
|com.acgist.snail.net|网络协议|
|com.acgist.snail.protocol|下载协议|
|com.acgist.snail.repository|持久层|
|com.acgist.snail.system|系统|

#### BitTorrent
[BitTorrent](http://www.bittorrent.org/beps/bep_0000.html)
[Kademlia、DHT、KRPC、BitTorrent 协议、DHT Sniffer](https://www.cnblogs.com/LittleHann/p/6180296.html)

#### 依赖模块
jdeps --list-deps *.jar

#### 使用
win：start.bat、SnailLauncher.exe、SnailLauncher_auto.exe，随便选择一个双击运行。

#### 文档
[帮助](https://gitee.com/acgist/snail/wikis/帮助)

#### TODO
dht：http://www.bittorrent.org/beps/bep_0005.html
local search：http://www.bittorrent.org/beps/bep_0014.html
