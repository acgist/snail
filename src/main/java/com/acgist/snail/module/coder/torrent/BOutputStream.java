package com.acgist.snail.module.coder.torrent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BOutputStream { /// 一个B编码输出类

	private ByteArrayOutputStream out = new ByteArrayOutputStream(); // 全局定义 一个预读byte输出流

	public byte[] Edi(Object infonodes) throws IOException { /// 返回一个处理过后的byte数组， 而Object则是info的Map。

		encoding(infonodes); // 调用下面方法

		byte[] b = null;

		if (out != null) {

			b = out.toByteArray(); // 把预读输出流的数据，转成一个整体byte

			out.flush();

			out.close();

		}

		return b;

	}

	// 重点来了 将内容编码进预读输入流里
	private void encoding(Object beans) throws IOException {

		if (beans instanceof LinkedHashMap) { // instanceof 用来判断对象是什么类型

			DMap((Map<String, Object>) beans); // LinkedHashMap 它是以插入顺序来排列的

		} else if (beans instanceof ArrayList) {

			BList((List<Object>) beans); // ArrayList 跟Map同理。

		} else if (beans instanceof byte[]) {

			Bbytes(beans);

		} else if (beans instanceof String) {

			BString(beans);

		} else if (beans instanceof Long) {

			BLong(beans);

		}

	}

	// Map就是那个字典 D E 编码
	private void DMap(Map<String, Object> node) throws IOException {

		out.write("d".getBytes()); // 编码码内容输出进 预读流中 编码内容一定全部都是byte数组。

		for (String key : node.keySet()) {
			/// 字符串长度 : 字符串内容 字典的解码时就是这概念，反过来编码用理。
			out.write((key.length() + ":" + key).getBytes());

			encoding(node.get(key)); // 继续去寻找类型 调用上面的方法。

		}

		out.write("e".getBytes()); // d开头 e结尾

	}

	// String解码 内容长度 : 内容 解码那时麻烦，编码容易。
	private void BString(Object node) throws IOException {
		// 一句话搞定 将它们以解码方式那样，重新组合之后转byte数组
		out.write((node.toString().length() + ":" + node.toString()).getBytes());

	}

	// 列表 = List解码 l 内容 e 编码也容易。
	private void BList(List<Object> nodes) throws IOException {

		out.write("l".getBytes()); // 开头

		for (Object kns : nodes) {

			encoding(kns); // 如果有内容继续去寻找类型 调用上面的方法。

		}

		out.write("e".getBytes()); // 结束

	}

	// 整数我用的是Long去解码的，当然你可以用Integer。 我拍Ineger放不下而已，拿个Long全放了。
	// 整数 = Integer or long 解码时 i 589 e 编码同理，重新组合。
	private void BLong(Object n) throws IOException {
		// 开头 内容 结尾 将它们全转成byte数组。
		out.write(("i" + Long.valueOf(n.toString()) + "e").getBytes());

	}

	// 上面的主要的4个类型就结束了 ，因为还有个二进制 byte数组类型 其实跟字符串同理
	// 一般就是用来处理pieces字段，因为那字段里全是二进制。
	private void Bbytes(Object b) throws IOException {

		out.write(String.valueOf(((byte[]) b).length).getBytes()); // 跟字符串编码方式一样

		out.write(":".getBytes());

		out.write((byte[]) b); // 只是最后直接输出它自身，且不需要转换。

	}
	
	public static void main(String[] args) {
		BOutputStream s = new BOutputStream();
//		s.Edi(infonodes)
	}

}
