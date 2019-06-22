package com.acgist.snail.dht;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.acgist.snail.net.bt.dht.DhtClient;
import com.acgist.snail.net.bt.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class DhtClientTest {

	@Test
	public void client() {
		InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", SystemConfig.getBtPort());
		DhtClient client = DhtClient.newInstance(socketAddress);
		while (true) {
			try {
				final Map<String, String> data = new HashMap<String, String>();
				data.put("v", "1.0.0");
				System.out.println("发送消息：" + data);
				client.send(ByteBuffer.wrap(BCodeEncoder.encodeMap(data)));
			} catch (NetException e) {
				e.printStackTrace();
			}
			ThreadUtils.sleep(1000);
		}
	}
	
	private static final String host = "127.0.0.1";
	private static final int port = 49160; // FDM测试端口
//	private static final String host = "router.bittorrent.com";
//	private static final int port = 6881;
	
	@Test
	public void ping() {
		NodeManager.getInstance().newNodeSession(host, port);
		DhtClient client = DhtClient.newInstance(host, port);
		NodeSession node = client.ping();
		System.out.println(node);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void findNode() {
		DhtClient client = DhtClient.newInstance(host, port);
		final String target = "5E5324691812EAA0032EA76E813CCFC4D04E7E9E";
		client.findNode(target);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void getPeers() throws DownloadException {
		DhtClient client = DhtClient.newInstance(host, port);
		final String hash = "16b1233b33143700fe47910898fcaaf0f05d2d09";
		final InfoHash infoHash = InfoHash.newInstance(hash);
		client.getPeers(infoHash);
		while(true) {
			var nodes = NodeManager.getInstance().findNode(hash);
			nodes.forEach(node -> {
				DhtClient clientx = DhtClient.newInstance(node.getHost(), node.getPort());
				clientx.getPeers(infoHash);
			});
			ThreadUtils.sleep(5000);
		}
//		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void getPeersOther() throws DownloadException {
		DhtClient client = DhtClient.newInstance("192.168.1.1", 1863);
		final String hash = "c15417e6aeab33732a59085d826edd29978f9afa";
		final InfoHash infoHash = InfoHash.newInstance(hash);
		client.getPeers(infoHash);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
