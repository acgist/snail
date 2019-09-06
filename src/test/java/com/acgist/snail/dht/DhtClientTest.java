package com.acgist.snail.dht;

import org.junit.Test;

import com.acgist.snail.net.torrent.dht.DhtClient;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

public class DhtClientTest {

	private static final String HOST = "127.0.0.1";
	private static final int PORT = 49160; // FDM测试端口
	
//	private static final String HOST = "router.bittorrent.com";
//	private static final int PORT = 6881;
	
	@Test
	public void ping() {
		NodeManager.getInstance().newNodeSession(HOST, PORT);
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		NodeSession node = client.ping();
		System.out.println(node);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void findNode() {
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		final String target = "5E5324691812EAA0032EA76E813CCFC4D04E7E9E";
		client.findNode(target);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void getPeers() throws DownloadException {
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		final String hash = "5E5324691812EAA0032EA76E813CCFC4D04E7E9E";
		final InfoHash infoHash = InfoHash.newInstance(hash);
		client.getPeers(infoHash);
		while(true) {
			var nodes = NodeManager.getInstance().findNode(hash);
			nodes.forEach(node -> {
				DhtClient nodeClient = DhtClient.newInstance(node.getHost(), node.getPort());
				nodeClient.getPeers(infoHash);
			});
			ThreadUtils.sleep(5000);
		}
	}
	
}
