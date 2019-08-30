# 帮助（常见问题）

如果遇到其他问题或者建议请提交[Issues](https://gitee.com/acgist/snail/issues)。

#### 环境
Java：11+  
Maven：3.6.0+  
系统支持：win、mac、linux

#### BT任务无法下载

* 网络故障
* 无人分享（做种）
* 查找不到Peer（没有可用的Tracker服务器和DHT网络）

#### 磁力链接无法下载

磁力链接下载使用Tracker服务器和DHT网络先加载Peer，然后连接Peer进行种子交换，然后在进行下载。  
所以如果磁力链接里面没有自带Tracker服务器并且也没有配置Tracker服务器和DHT网络时就会导致磁力链接无法下载。  
即使配置了Tracker服务器和DHT网络，磁力链接在转种子时还是会出现长时间查找Peer导致的等待。

#### BT任务分享

BT任务上传速度限制和下载速度限制一致，软件启动后不论任务状态（下载中、暂停、完成）都会进行分享，直到软件关闭或者任务被删除。

#### 无法分享（做种）

* 是否处于多路由环境。
* 是否接入了多条网络。
* 是否开启路由器UPNP功能。

#### 优化下载体验

使用热门种子下载，自定义优质的Tracker服务器和DHT网络。

#### 自定义Tracker服务器

在软件根目录创建文件/config/bt.tracker.properties，配置键值对：index=AnnounceUrl  
> index=任意值  
> AnnounceUrl=Tracker服务器地址

#### 自定义DHT网络

在软件根目录创建文件/config/bt.dht.properties，配置键值对：nodeID=host:port 
> nodeID=节点ID  
> host:port=服务器地址（域名|IP）:端口