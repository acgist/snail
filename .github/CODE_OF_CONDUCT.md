# 代码规范

总则：让代码规范、可读、优美

新增代码必须遵循以下规范，更多参考[阿里巴巴Java开发手册](https://github.com/alibaba/p3c)

## 目录

* [缩进](#缩进)
* [括号](#括号)
* [换行](#换行)
* [注释](#注释)
* [命名](#命名)
	* [类命名](#类命名)
	* [变量命名](#变量命名)
* [代码](#代码)
* [检查工具](#检查工具)
	* [规范检查](#规范检查)
	* [质量检查](#质量检查)

## 缩进

使用Tab缩进

## 括号

```
// 即是条件只有一行也不能省略大括号
if(条件) {
}
```

## 换行

```
// 语句太长换行
if(
	条件 &&
	条件
) {
} else if(条件) {
} else {
}
// Lambda换行
list.stream()
	.filter(peer -> peer.available())
	.filter(peer -> peer.connected())
	.limit(DhtConfig.GET_PEER_SIZE)
	.map(peer -> {
		buffer.putInt(NetUtils.encodeIpToInt(peer.host()));
		buffer.putShort(NetUtils.encodePort(peer.port()));
		buffer.flip();
		return buffer.array();
	})
	.collect(Collectors.toList());
```

## 注释

* 类、接口必须使用javadoc注解：用途、作者、版本
* 抽象方法必须使用javadoc注解：用途、参数、返回值、异常
* 类变量、静态变量必须使用javadoc注解
* 注释文本和前面的符号必须有个空格

## 命名

* 必须使用英文
* 名称具有可读性
* 名称必须遵从驼峰命名
* 常量、枚举必须大写
* 不使用不规范的缩写

#### 包命名

不同功能的类和接口必须放到对应功能的包目录下

|包名|描述|
|:--|:--|
|downloader|下载器|
|gui|GUI|
|net|网络|
|pojo|POJO|
|protocol|协议|
|repository|数据库|
|system|系统|
|utils|工具|

#### 类命名

* 接口必须以`I`开头
* 测试必须`Test`结尾
* 抽象类不使用`Abstract`或`Base`开头，例：TrackerClient -> [UdpTrackerClient | HttpTrackerClient]
* 继承时子类命名必须以父类后缀结尾，例：Client、Server、MessageHandler、Downloader、Protocol、Repository等等

#### 变量命名

* 基本类型根据用途命名
* 其他类型使用`Eclipse`自动提示（类名首字母小写）

## 代码

* 不能使用过时的方法
* 能使用单例的类必须使用单例
* 不使用魔法值，使用常量代替
* 变量、类尽量使用`final`修饰
* `switch`语句必须包含`default`
* 方法重写时必须添加注解`@Override`
* 声明`long`类型时，数值后面添加`L`标记
* 异常如果没有抛出必须使用使用日志框架记录
* 必须处理所有警告，不允许通过注解`@SuppressWarnings`忽略

```
// 数组
final int[] peers = new int[8];

// 判断
if(a == b) {
}

// 三目运算
final int value = boolean ? a : b;

// 参数传递
this.method(arg1, arg2, arg3);

// 可变参数
public String method(String ... args);
```

## 检查工具

#### 规范检查

使用阿里P3C对代码进行规范检查

###### P3C

https://github.com/alibaba/p3c

###### Eclipse

https://p3c.alibaba.com/plugin/eclipse/update

#### 质量检查

使用Sonar对代码进行质量检查

```
mvn sonar:sonar "-Dsonar.projectKey=snail" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.login=token"
```