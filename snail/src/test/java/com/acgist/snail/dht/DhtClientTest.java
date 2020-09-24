package com.acgist.snail.dht;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.exception.DownloadException;
import com.acgist.snail.net.torrent.dht.DhtClient;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.utils.ThreadUtils;

public class DhtClientTest extends BaseTest {

	// 本地FDM测试节点
//	private static final int PORT = 49160;
//	private static final String HOST = "127.0.0.1";
	// DHT节点
	private static final int PORT = 6881;
	private static final String HOST = "router.bittorrent.com";
	// 种子HASH
	private static final String HASH = "5E5324691812EAA0032EA76E813CCFC4D04E7E9E";
	
	@Test
	public void testPing() {
		final var client = DhtClient.newInstance(HOST, PORT);
		final var node = client.ping();
		this.log("节点信息：{}", node);
	}
	
	@Test
	public void testFindNode() {
		final var client = DhtClient.newInstance(HOST, PORT);
		client.findNode(HASH);
		this.pause();
	}
	
	@Test
	public void testGetPeers() throws DownloadException {
		final var client = DhtClient.newInstance(HOST, PORT);
		final var infoHash = InfoHash.newInstance(HASH);
		client.getPeers(infoHash);
		while(true) {
			NodeManager.getInstance().findNode(HASH).forEach(node -> {
				final DhtClient nodeClient = DhtClient.newInstance(node.getHost(), node.getPort());
				nodeClient.getPeers(infoHash);
			});
			this.log("系统节点数量：{}", NodeManager.getInstance().nodes().size());
			ThreadUtils.sleep(5000);
		}
	}
	
}
