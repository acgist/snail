package com.acgist.snail.protocol.magnet.bootstrap;

import java.io.File;

import com.acgist.snail.net.bt.dht.DhtClient;
import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.exception.DownloadException;
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
	private final Magnet magnet;
	private final NodeManager nodeManager;
	
	private TorrentLoader(String url) throws DownloadException {
		this.magnet = MagnetReader.newInstance(url).magnet();
		this.hash = StringUtils.unhex(this.magnet.getHash());
		this.nodeManager = NodeManager.getInstance();
	}
	
	public static final TorrentLoader newInstance(String url) throws DownloadException {
		return new TorrentLoader(url);
	}
	
	public File load() {
		final var nodes = this.nodeManager.findNode(this.hash);
		for (NodeSession node : nodes) {
			load(node);
		}
		return null;
	}
	
	private void load(NodeSession node) {
		final DhtClient client = DhtClient.newInstance(node.getHost(), node.getPort());
		client.getPeers(this.hash);
	}
	
}
