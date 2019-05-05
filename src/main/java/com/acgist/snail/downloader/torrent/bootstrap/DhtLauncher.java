package com.acgist.snail.downloader.torrent.bootstrap;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * DHT查询
 */
public class DhtLauncher {

	/**
	 * 由外界添加，非自动查询添加Node，例如：种子文件
	 */
	private Queue<InetSocketAddress> address = new LinkedBlockingDeque<>();
	
}
