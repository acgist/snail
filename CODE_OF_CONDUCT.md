# 代码规范

提交PR、问题建议[Issues](https://gitee.com/acgist/snail/issues)前请大家阅读本代码规范

谢谢！

总则：规范、可读、优美

## 目录

* [缩进](#缩进)
* [括号](#括号)
* [换行](#换行)
* [注释](#注释)
* [日志](#日志)
* [命名](#命名)
	* [版本号](#版本号)
	* [包命名](#包命名)
	* [类命名](#类命名)
	* [方法命名](#方法命名)
	* [变量命名](#变量命名)
* [代码](#代码)
* [顺序](#顺序)
* [检测工具](#检测工具)
	* [规范检测](#规范检测)
	* [质量检测](#质量检测)

## 缩进

使用Tab缩进

## 括号

* 条件禁止判断省略括号
* 单句函数推荐省略括号

```
if(条件)  {
}

do {
} while(条件);

Function function = event -> {
...
};

Function function = event -> sentence;
```

## 换行

* 复杂条件建议换行
* 语句过长建议换行

```
if(
	条件 &&
	条件
) {
} else if(条件) {
} else {
}

list.stream()
	.filter(PeerSession::available)
	.filter(PeerSession::connected)
	.limit(DhtConfig.GET_PEER_SIZE)
	.map(peer -> {
		...
		return buffer.array();
	})
	.collect(Collectors.toList());
```

## 注释

* 建议不要使用行尾注释
* javadoc注解必须正确使用
* 重要功能代码必须添加注释
* 协议实现尽量注释协议链接

### 方法注解

* 直接引用方法添加注解`@see`

## 日志

* 合理使用日志级别
* 必须使用日志门面
* 重要操作必须添加日志
* 异常没有抛出必须添加日志

## 命名

* 必须使用英文
* 必须使用驼峰命名
* 必须大写常量、枚举
* 名称具有可读性（不使用不规范缩写）

### 版本号

#### 规则

`主版本号.Java版本号.修订号`

###### 示例

`1.11.0`、`1.17.0`、`1.17.1`

#### 稳定版本

稳定版本（Release分支）只有使用Java长期支持版本（TLS）才会发布，同时发布Maven中央仓库。
其他版本发布标签（Tag）不会发布Maven中央仓库。

### 包命名

包功能描述

|包名|描述|
|:--|:--|
|config|配置|
|context|上下文|
|downloader|下载器|
|format|格式|
|gui|Gui|
|logger|日志|
|net|网络|
|pojo|POJO|
|protocol|协议|
|utils|工具|

### 类命名

#### 接口

接口必须使用`I`开头

###### 实例

`ITaskSession` -> `TaskSession`

#### 抽象类

* 不使用`base`、`Abstract`开头
* 子类必须使用父类后缀结尾
* 构造方法必须使用`protected`修饰

###### 示例

`TrackerClient` -> [`UdpTrackerClient` | `HttpTrackerClient`]

#### 工具助手

* 助手类使用`s`结尾
* 工具类使用`Utils`结尾

###### 实例

```java
// 字体助手
Fonts
// 时间工具
DateUtils
```

#### 测试类

测试类必须使用`Test`结尾

### 方法命名

#### Getter

* 纯数据类自动生成
* 其他类建议直接使用变量名称作为Getter

#### Setter

* 纯数据类自动生成
* 其他类建议直接使用变量名称作为Setter

#### 测试方法

测试方法必须使用`test`开头

### 变量命名

* 基本类型根据用途命名
* 其他类型建议根据功能命名或使用`Eclipse`自动提示

## 代码

* 尽量使用单例
* 禁止使用魔法值
* 禁止使用过时的方法
* 尽量使用`final`修饰类、变量
* 重写方法必须添加注解`@Override`
* 使用`switch`语句必须包含`default`
* 声明`long`类型数值后面添加`L`标记（`float`、`double`类似）
* 所有警告必须处理（禁止通过注解`@SuppressWarnings`忽略）

## 顺序

* 日志
* 单例
* 枚举
* 静态常量
* 静态代码
* 全局变量
* 构造函数
* 初始代码
* 公开函数
* 私有函数

## 检测工具

### 规范检测

使用阿里[P3C](https://github.com/alibaba/p3c)对代码进行规范检测（[Eclipse插件](https://p3c.alibaba.com/plugin/eclipse/update)）

### 质量检测

* 使用Sonar对代码进行质量检测
* 使用JaCoCo对代码进行覆盖率检测
