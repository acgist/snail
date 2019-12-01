package com.acgist.snail.dht;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.dht.DhtClient;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

public class DhtClientTest extends BaseTest {

	private static final String HOST = "127.0.0.1";
	private static final int PORT = 49160; // FDM测试端口
	
//	private static final String HOST = "router.bittorrent.com";
//	private static final int PORT = 6881;
	
	@Test
	public void testPing() {
//		NodeManager.getInstance().newNodeSession(HOST, PORT);
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		NodeSession node = client.ping();
		this.log(node);
	}
	
	@Test
	public void testFindNode() {
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		String target = "5E5324691812EAA0032EA76E813CCFC4D04E7E9E";
		client.findNode(target);
		this.pause();
	}
	
	@Test
	public void testGetPeers() throws DownloadException {
		DhtClient client = DhtClient.newInstance(HOST, PORT);
		String hash = "5E5324691812EAA0032EA76E813CCFC4D04E7E9E";
		InfoHash infoHash = InfoHash.newInstance(hash);
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
