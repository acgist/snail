package com.acgist.snail.dht;

import org.junit.Test;

import com.acgist.snail.net.dht.DhtClient;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

public class DhtServerTest {

	private static final String host = "127.0.0.1";
	private static final int port = 18888; // 本地DHT测试端口
	
	@Test
	public void ping() {
		DhtClient client = DhtClient.newInstance(host, port);
		NodeSession node = client.ping();
		System.out.println(node);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void findNode() {
		DhtClient client = DhtClient.newInstance(host, port);
		final String target = "5E5324691812CAA0032EA76E813CCFC4D04E7E9E";
		client.findNode(target);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void getPeers() throws DownloadException {
		DhtClient client = DhtClient.newInstance(host, port);
		final String target = "5E5324691812CAA0032EA76E813CCFC4D04E7E9E";
		final InfoHash infoHash = InfoHash.newInstance(target);
		client.getPeers(infoHash);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void announcePeer() throws DownloadException {
		DhtClient client = DhtClient.newInstance(host, port);
		final String target = "5E5324691812CAA0032EA76E813CCFC4D04E7E9E";
		final InfoHash infoHash = InfoHash.newInstance(target);
		client.announcePeer("1234".getBytes(), infoHash);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
