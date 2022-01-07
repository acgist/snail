package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.acgist.snail.context.NodeContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>DHT定时任务</p>
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
	 * <p>Peer客户端节点队列</p>
	 * <p>支持DHT协议的Peer客户端节点</p>
	 */
	private final List<InetSocketAddress> peerNodes;
	
	/**
	 * @param torrentSession BT任务信息
	 */
	private DhtLauncher(TorrentSession torrentSession) {
		this.infoHash = torrentSession.infoHash();
		this.peerNodes = new ArrayList<>();
	}
	
	/**
	 * <p>新建DHT定时任务</p>
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
		final List<InetSocketAddress> nodes;
		synchronized (this.peerNodes) {
			nodes = new ArrayList<>(this.peerNodes);
			this.peerNodes.clear();
		}
		try {
			final var list = this.pick();
			if(CollectionUtils.isNotEmpty(nodes)) {
				this.joinNodes(nodes);
				list.addAll(nodes);
			}
			this.findPeers(list);
		} catch (Exception e) {
			LOGGER.error("执行DHT定时任务异常", e);
		}
	}
	
	/**
	 * <p>添加Peer客户端节点</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @see #peerNodes
	 */
	public void put(String host, Integer port) {
		synchronized (this.peerNodes) {
			this.peerNodes.add(NetUtils.buildSocketAddress(host, port));
		}
	}
	
	/**
	 * <p>挑选DHT节点</p>
	 * 
	 * @return DHT节点
	 */
	private List<InetSocketAddress> pick() {
		return NodeContext.getInstance().findNode(this.infoHash.infoHash()).stream()
			.filter(NodeSession::markVerify)
			.map(node -> NetUtils.buildSocketAddress(node.getHost(), node.getPort()))
			.collect(Collectors.toList());
	}

	/**
	 * <p>将Peer客户端节点加入到系统节点</p>
	 * 
	 * @param peerNodes Peer客户端节点
	 * 
	 * @see #peerNodes
	 */
	private void joinNodes(List<InetSocketAddress> peerNodes) {
		final NodeContext nodeContext = NodeContext.getInstance();
		peerNodes.forEach(address -> nodeContext.newNodeSession(address.getHostString(), address.getPort()));
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
		final byte[] infoHashValue = this.infoHash.infoHash();
		for (InetSocketAddress socketAddress : list) {
			DhtClient.newInstance(socketAddress).getPeers(infoHashValue);
		}
	}
	
}
