package com.acgist.snail.net.dht;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.system.bcode.BCodeDecoder;

/**
 * http://www.bittorrent.org/beps/bep_0005.html
 */
public class DhtMessageHandler extends UdpMessageHandler {

	@Override
	public void onMessage(InetSocketAddress address, ByteBuffer buffer) {
		
	}
	
	/**
	 * 检测节点是否可达
	 */
	public void ping() {
	}

	/**
	 * 查找给定ID的DHT节点的联系信息，回复被请求节点的路由表中距离请求target最接近的K个node的ID和INFO
	 */
	public void findNode() {
	}

	/**
	 * 获取Peer
	 */
	public void getPeers() {
	}

	/**
	 * 表明发出announce_peer请求的节点，正在某个端口下载torrent文件
	 */
	public void announcePeer() {
	}
	
	private void contactEncoding() {
	}

	private void response(byte[] bytes) {
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final Map<String, Object> response = decoder.mustMap();
		
	}

	/**
	 * 错误：e是一个列表：
	 * 	[0]：错误代码：
	 * 		201：一般错误
	 * 		202：服务错误
	 * 		203：协议错误，不规范的包、无效参数、错误token
	 * 		204：未知方法
	 * 	[1]：错误描述
	 */
	private void error() {
	}

}
