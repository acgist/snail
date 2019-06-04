package com.acgist.snail.downloader.torrent.bootstrap;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.dht.DhtClient;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>DHT任务：定时查询Peer</p>
 * <p>BT下载任务客户端连接时如果支持DHT，放入到{@link #dhtAddress}列表。</p>
 * <p>定时使用最近的可用节点和{@link #dhtAddress}查询Peer。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtLauncher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtLauncher.class);
	
	private final InfoHash infoHash;
	
	/**
	 * 客户端连接时支持DHT，加入列表，定时查询Peer时使用。
	 */
	private Queue<InetSocketAddress> dhtAddress = new LinkedBlockingDeque<>();
	
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
			findPeers(list);
		} catch (Exception e) {
			LOGGER.error("DHT任务异常", e);
		}
	}

	/**
	 * 选择DHT客户端地址
	 */
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
				list.add(NetUtils.buildSocketAddress(node.getHost(), node.getPort()));
			}
		}
		return list;
	}
	
	/**
	 * 查询Peer
	 */
	private void findPeers(List<InetSocketAddress> list) {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		for (InetSocketAddress socketAddress : list) {
			final DhtClient client = DhtClient.newInstance(socketAddress);
			client.getPeers(infoHash.infoHash());
		}
	}
	
	/**
	 * Peer客户端添加DHT客户端
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public void put(String host, Integer port) {
		LOGGER.debug("添加DHT客户端：{}-{}", host, port);
		this.dhtAddress.add(NetUtils.buildSocketAddress(host, port));
	}
	
}
