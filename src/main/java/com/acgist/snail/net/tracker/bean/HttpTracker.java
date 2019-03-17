package com.acgist.snail.net.tracker.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP Tracker返回
 */
public class HttpTracker {

	public static final List<String> KEYS = new ArrayList<>();

	static {
		KEYS.add("failure reason"); // 失败原因
		KEYS.add("warnging message"); // 警告信息
		KEYS.add("interval"); // 下一次连接等待时间
		KEYS.add("min interval"); // 下一次连接等待最小时间
		KEYS.add("tracker id"); // tracker id
		KEYS.add("complete"); // 当前有多少个peer已经完成了整个共享文件的下载
		KEYS.add("incomplete"); // 当前有多少个peer还没有完成共享文件的下载
		KEYS.add("peers"); // 各个peer的IP和端口号
	}

}
