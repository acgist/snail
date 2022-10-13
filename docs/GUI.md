# 界面操作

## 主界面

* 可以通过拖动下载链接或种子文件进入界面进行下载
* 如果软件意外关闭可以使用文件校验修复BT任务文件
* 如果任务文件删除可以使用文件修复重新开始下载任务
* 删除：可以通过[设置](#设置)配置删除文件时是否同时删除任务文件

![主界面](.//gui/main.png "主界面")

## 新建下载

* 可以通过拖动下载链接或种子文件进入界面进行下载

![新建下载](.//gui/build.png "新建下载")

## 设置

* 下载速度：限制单个任务下载速度
* 删除文件：删除任务时是否同时删除任务文件
* 下载目录：点击目录文本可以快速打开下载目录
* 消息提示：下载完成或下载失败时弹出系统提示

![设置](.//gui/setting.png "设置")

## 关于

扫描二维码捐助🐒

![关于](.//gui/about.png "关于")

## 统计

主界面按`F12`进入统计页面

### 系统信息

![系统信息](.//gui/statistics-system.png "系统信息")

### DHT节点统计

* 可用：可以使用
* 未知：没有使用
* 验证：发送请求没有响应

![DHT节点统计](.//gui/statistics-node.png "DHT节点统计")

### Tracker统计

![Tracker统计](.//gui/statistics-tracker.png "Tracker统计")

### Peer类型统计

* 点击饼图可以隐藏未知类型Peer统计
* 未知类型Peer：没有连接或连接失败的Peer

![Peer类型统计](.//gui/statistics-client.png "Peer类型统计")

### Peer来源统计

![Peer来源统计](.//gui/statistics-source.png "Peer来源统计")

### Peer连接统计

![Peer连接统计](.//gui/statistics-connect.png "Peer连接统计")

### Peer流量统计

* 交战（黄色）：上传和下载
* 上传（红色）：只上传
* 下载（绿色）：只下载
* 无情（灰色）：没有数据交流

![Peer流量统计](.//gui/statistics-traffic.png "Peer流量统计")

### Piece统计

* 已下载（绿色）：已经下载完成
* 未下载（黄色）：选择下载还未下载
* 不下载（灰色）：没有选择下载
* 鼠标可以通过点击指定优先下载位置
* 健康度：当前任务所选文件是否全部能够下载（健康度不是100%肯定不能下载成功）

![Piece统计](.//gui/statistics-piece.png "Piece统计")
