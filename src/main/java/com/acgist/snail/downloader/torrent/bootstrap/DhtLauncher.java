package com.acgist.snail.downloader.torrent.bootstrap;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.dht.DhtClient;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.utils.CollectionUtils;

/**
 * DHT查询：定时任务
 */
public class DhtLauncher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtLauncher.class);
	
	/**
	 * 由外界添加，非自动查询添加Node，例如：种子文件
	 */
	private Queue<InetSocketAddress> dhtAddress = new LinkedBlockingDeque<>();
	
	private final InfoHash infoHash;
	
	private DhtLauncher(TorrentSession session) {
		this.infoHash = session.infoHash();
	}
	
	public static final DhtLauncher newInstance(TorrentSession session) {
		return new DhtLauncher(session);
	}
	
	@Override
	public void run() {
		LOGGER.debug("执行DHT定时任务");
		try {
			final var list = pick();
			execute(list);
		} catch (Exception e) {
			LOGGER.error("DHT任务异常", e);
		}
	}

	private List<InetSocketAddress> pick() {
		final List<InetSocketAddress> list = new ArrayList<>();
		while(true) {
			final var address = this.dhtAddress.poll();
			if(address == null) {
				break;
			}
			list.add(address);
		}
		final var nodes = NodeManager.getInstance().findNode(infoHash.infoHash());
		if(CollectionUtils.isNotEmpty(nodes)) {
			for (NodeSession node : nodes) {
				list.add(new InetSocketAddress(node.getHost(), node.getPort()));
			}
		}
		return list;
	}
	
	private void execute(List<InetSocketAddress> list) {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		for (InetSocketAddress address : list) {
			final DhtClient client = DhtClient.newInstance(address);
			client.getPeers(infoHash.infoHash());
		}
	}
	
	/**
	 * 添加DHT客户端
	 * @param host 地址
	 * @param port 端口
	 */
	public void put(String host, Integer port) {
		LOGGER.debug("添加DHT客户端：{}-{}", host, port);
		this.dhtAddress.add(new InetSocketAddress(host, port));
	}
	
}
