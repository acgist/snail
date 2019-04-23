# Snail（蜗牛）

## 介绍
基于JAVA/JAVAFX的下载工具，支持下载协议：BT、FTP、HTTP、ED2K。

## 技术
|技术|版本|
|:-|:-|
|JAVA|11|
|JAVAFX|12|

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

*说明：√=完成、○-进行中、×-未开始*

## TODO
PEX：http://www.bittorrent.org/beps/bep_0011.html   
DHT：http://www.bittorrent.org/beps/bep_0005.html   
UTP：http://www.bittorrent.org/beps/bep_0029.html   
local search：http://www.bittorrent.org/beps/bep_0014.html   

## BitTorrent
[BitTorrent](http://www.bittorrent.org/beps/bep_0000.html)   
[Kademlia、DHT、KRPC、BitTorrent 协议、DHT Sniffer](https://www.cnblogs.com/LittleHann/p/6180296.html)

## 文档
[帮助](https://gitee.com/acgist/snail/wikis/帮助)
