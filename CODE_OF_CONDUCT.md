# 代码规范

总则：让代码规范、可读、优美

新增代码必须遵循以下规范

更多参考[阿里巴巴Java开发手册](https://github.com/alibaba/p3c)

## 目录

* [缩进](#缩进)
* [括号](#括号)
* [换行](#换行)
* [注释](#注释)
* [日志](#日志)
* [命名](#命名)
	* [类命名](#类命名)
	* [变量命名](#变量命名)
* [代码](#代码)
* [顺序](#顺序)
* [检查工具](#检查工具)
	* [规范检查](#规范检查)
	* [质量检查](#质量检查)

## 缩进

使用Tab缩进

## 括号

```
// 不能省略括号
if(条件) {
}

do {
} while(条件);

Function function = (event) -> {
};
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
		...
		return buffer.array();
	})
	.collect(Collectors.toList());
```

## 注释

* 允许同行注释
* 重要功能和代码必须添加注释
* 注释文本和前面的符号必须有个空格
* 类变量、静态变量必须使用javadoc注解
* 单例对象、获取单例方法、无参构造函数不要注释
* 类、接口必须使用javadoc注解：描述、作者
* 抽象方法必须使用javadoc注解：描述、参数、返回值、异常

## 日志

* 合理使用日志级别
* 重要操作必须添加日志
* 必须使用日志门面（slf4j）
* 异常没有抛出必须添加日志记录

## 命名

* 必须使用英文
* 必须具有可读性
* 必须遵从驼峰命名
* 常量、枚举必须大写
* 不使用不规范的缩写

#### 包命名

不同功能的类和接口必须放到对应功能的包目录下

|包名|描述|
|:--|:--|
|config|配置|
|context|上下文|
|downloader|下载器|
|format|数据格式|
|gui|GUI|
|net|网络|
|pojo|POJO|
|protocol|协议|
|repository|数据库|
|utils|工具|

#### 类命名

* 接口必须以`I`开头
* 测试必须以`Test`结尾
* 抽象类不使用`Abstract`或`Base`开头（例：TrackerClient -> [UdpTrackerClient | HttpTrackerClient]）
* 继承时子类命名必须以父类后缀结尾（例：Client、Server、MessageHandler、Downloader、Protocol、Repository等）

#### 变量命名

* 基本类型根据用途命名
* 其他类型使用`Eclipse`自动提示（类名首字母小写）

## 代码

* 不能使用过时的方法
* 能使用单例必须使用单例
* 不使用魔法值（使用常量代替）
* 类、变量尽量使用`final`修饰
* 方法重写时必须添加注解`@Override`
* `switch`语句必须包含`default`
* 新增代码不能使用除了Java内置模块和已经添加依赖外的其他依赖
* 必须处理所有警告（不允许通过注解`@SuppressWarnings`忽略）
* 声明`long`类型时数值后面添加`L`标记（`float`、`double`类似）

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

// 是否存在方法命名使用haveXXXX
public String haveData();
```

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

> 函数按照相关引用排序

## 检查工具

#### 规范检查

使用阿里P3C对代码进行规范检查

###### P3C

https://github.com/alibaba/p3c

###### Eclipse

https://p3c.alibaba.com/plugin/eclipse/update

#### 质量检查

* 使用Sonar对代码进行质量检查
* 使用JaCoCo对代码进行覆盖率检测