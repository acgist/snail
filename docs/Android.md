# 安卓

安卓移植需要改写两个问题：文件权限、适配`JDK`新的`API`

## 文件权限

安卓文件权限比较严格，需要修改以下三个地方文件位置：

* 日志文件
* 下载文件
* 配置文件

## 适配JDK新的API

安卓已经支持`JDK17`，但是部分`API`没有实现，所以需要重写源码，已知`API`：

### 没有实现

* `InputStream`没有实现`transferTo`
* `InputStream`没有实现`readAllBytes`
* `NetworkInterface`没有实现`networkInterfaces`

### 实现方式

* `OutputStream`创建文件`createNewFile`
* `DatagramChannel`绑定端口`bind`