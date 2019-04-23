# Snail（蜗牛）

#### 介绍
基于JAVA/JAVAFX的下载工具，支持下载协议：BT、FTP、HTTP、ED2K。

#### 进度
|功能|进度|
|:-|:-|
|HTTP|√|
|FTP|√|
|BT|○|
|ED2K|×|

###### BT进度
|协议|进度|
|:-|:-|
|Tracker Protocol（UDP/HTTP）|√|
|Peer wire protocol（TCP）|√|
|uTorrent transport protocol（uTP）|×|
|Extension for Peers to Send Metadata Files|√|
|Peer Exchange（PEX）|×|
|DHT Protocol|×|
|Local Service Discovery|×|

*说明：√=完成、○-进行中、×-未开始*

#### 技术
|技术|版本|
|:-|:-|
|JAVA|11|
|JAVAFX|12|

#### BitTorrent
[BitTorrent](http://www.bittorrent.org/beps/bep_0000.html)   
[Kademlia、DHT、KRPC、BitTorrent 协议、DHT Sniffer](https://www.cnblogs.com/LittleHann/p/6180296.html)

#### 文档
[帮助](https://gitee.com/acgist/snail/wikis/帮助)

#### TODO
PEX：http://www.bittorrent.org/beps/bep_0011.html   
DHT：http://www.bittorrent.org/beps/bep_0005.html   
UTP：http://www.bittorrent.org/beps/bep_0029.html   
local search：http://www.bittorrent.org/beps/bep_0014.html   
