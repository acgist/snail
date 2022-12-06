# 代码规范

总则：规范、可读、优美

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

## 枚举转换

```
// 使用索引
private static final Type[] INDEX = EnumUtils.index(Type.class, Type::id);
if(value < 0 || value >= INDEX.length) {
	return null;
}
return INDEX[value];

// 使用switch
return switch (value) {
case 0x00 -> DATA;
case 0x01 -> FIN;
case 0x02 -> STATE;
case 0x03 -> RESET;
case 0x04 -> SYN;
default -> null;
};

// 使用for
final Type[] types = Type.values();
for (Type type : types) {
	if(type.type == value) {
		return type;
	}
}
return null;
```

> 使用索引必须连续数值

## 注释

* 建议不要使用行尾注释
* `javadoc`注解必须正确使用
* 重要功能代码必须添加注释
* 协议实现尽量注释协议链接
* 简单方法直接注释参数和返回值即可，例如：`of`/`getter`/`setter`

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

稳定版本（`Release`分支）只有使用`Java`长期支持版本（`TLS`）才会发布，同时发布`Maven`中央仓库。
其他版本发布标签（`Tag`）不会发布`Maven`中央仓库。

### 包命名

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
* 其他类建议直接使用变量名称作为`Getter`

#### Setter

* 纯数据类自动生成
* 其他类建议直接使用变量名称作为`Setter`

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

### 泛型

* 返回值不能直接返回`List<?>`和`Map<?, ?>`
* 入参为了适应任何数据类型可以使用`List<?>`和`Map<?, ?>`
* 除非为了防止警告以外定义变量不能直接使用`List<?>`和`Map<?, ?>`

## 顺序

* 日志常量
* 单例常量
* 枚举代码
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
