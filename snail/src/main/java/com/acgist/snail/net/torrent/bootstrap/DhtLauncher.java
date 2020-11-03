package com.acgist.snail.net.torrent.bootstrap;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.dht.DhtClient;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>DHT定时任务</p>
 * <p>定时使用系统最近的DHT节点和{@link #peerNodes}查询Peer</p>
 * 
 * @author acgist
 */
public final class DhtLauncher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtLauncher.class);
	
	/**
	 * <p>种子信息</p>
	 */
	private final InfoHash infoHash;
	/**
	 * <p>客户端节点队列</p>
	 * <p>如果连接的Peer支持DHT，将该Peer放入到队列，下次查询时使用同时加入到系统节点。</p>
	 */
	private final List<InetSocketAddress> peerNodes = new ArrayList<>();
	
	/**
	 * @param torrentSession BT任务信息
	 */
	private DhtLauncher(TorrentSession torrentSession) {
		this.infoHash = torrentSession.infoHash();
	}
	
	/**
	 * <p>创建DHT定时任务</p>
	 * 
	 * @param torrentSession BT任务信息
	 * 
	 * @return DhtLauncher
	 */
	public static final DhtLauncher newInstance(TorrentSession torrentSession) {
		return new DhtLauncher(torrentSession);
	}
	
	@Override
	public void run() {
		LOGGER.debug("执行DHT定时任务");
		List<InetSocketAddress> nodes;
		synchronized (this.peerNodes) {
			nodes = new ArrayList<>(this.peerNodes);
			this.peerNodes.clear(); // 清空节点信息
		}
		try {
			this.joinSystemNodes(nodes);
			final var list = this.pick(nodes);
			this.findPeers(list);
		} catch (Exception e) {
			LOGGER.error("执行DHT定时任务异常", e);
		}
	}
	
	/**
	 * <p>将Peer客户端加入客户端节点队列</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public void put(String host, Integer port) {
		synchronized (this.peerNodes) {
			this.peerNodes.add(NetUtils.buildSocketAddress(host, port));
		}
	}

	/**
	 * <p>将客户端节点队列中的DHT节点加入系统节点</p>
	 * 
	 * @param peerNodes 客户端节点队列
	 */
	private void joinSystemNodes(List<InetSocketAddress> peerNodes) {
		if(CollectionUtils.isNotEmpty(peerNodes)) {
			peerNodes.forEach(
				address -> NodeManager.getInstance().newNodeSession(address.getHostString(), address.getPort())
			);
		}
	}
	
	/**
	 * <p>挑选DHT节点</p>
	 * 
	 * @param peerNodes 客户端节点队列
	 * 
	 * @return DHT节点
	 */
	private List<InetSocketAddress> pick(List<InetSocketAddress> peerNodes) {
		final List<InetSocketAddress> nodes = new ArrayList<>();
		// 客户端节点
		if(CollectionUtils.isNotEmpty(peerNodes)) {
			nodes.addAll(peerNodes);
		}
		// 系统节点
		final var systemNodes = NodeManager.getInstance().findNode(this.infoHash.infoHash());
		if(CollectionUtils.isNotEmpty(systemNodes)) {
			for (NodeSession node : systemNodes) {
				nodes.add(NetUtils.buildSocketAddress(node.getHost(), node.getPort()));
			}
		}
		return nodes;
	}
	
	/**
	 * <p>使用DHT节点查询Peer</p>
	 * 
	 * @param list DHT节点
	 */
	private void findPeers(List<InetSocketAddress> list) {
		if(CollectionUtils.isEmpty(list)) {
			LOGGER.debug("DHT定时任务没有节点可用");
			return;
		}
		final byte[] infoHash = this.infoHash.infoHash();
		for (InetSocketAddress socketAddress : list) {
			DhtClient.newInstance(socketAddress).getPeers(infoHash);
		}
	}
	
}
