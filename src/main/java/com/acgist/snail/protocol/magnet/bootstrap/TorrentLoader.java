package com.acgist.snail.protocol.magnet.bootstrap;

import com.acgist.snail.net.bt.dht.DhtClient;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子加载器
 * 将磁力链接转换为种子文件。
 * 
 * @author acgist
 * @since 1.1.0
 */
public class TorrentLoader {

	private final byte[] hash;
	private final NodeManager nodeManager;
	
	private TorrentLoader(String hash) {
		this.hash = StringUtils.unhex(hash);
		this.nodeManager = NodeManager.getInstance();
	}
	
	public static final TorrentLoader newInstance(String hash) {
		return new TorrentLoader(hash);
	}
	
	public void load() {
		final var nodes = this.nodeManager.findNode(this.hash);
		for (NodeSession node : nodes) {
			load(node);
		}
	}
	
	private void load(NodeSession node) {
		final DhtClient client = DhtClient.newInstance(node.getHost(), node.getPort());
		client.getPeers(this.hash);
	}
	
}
