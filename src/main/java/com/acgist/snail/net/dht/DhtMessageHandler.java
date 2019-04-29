package com.acgist.snail.net.dht;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.net.dht.bootstrap.request.PingRequest;
import com.acgist.snail.system.exception.NetException;

/**
 * http://www.bittorrent.org/beps/bep_0005.html
 */
public class DhtMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtMessageHandler.class);

	@Override
	public void onMessage(InetSocketAddress address, ByteBuffer buffer) {
		LOGGER.debug("DHT消息，地址：{}", address);
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final Response response = Response.valueOf(bytes);
		System.out.println(response);
	}
	
	/**
	 * 检测节点是否可达
	 */
	public void ping(SocketAddress address) {
		final PingRequest request = PingRequest.newRequest();
		pushMessage(request, address);
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
	
	private void pushMessage(Request request, SocketAddress address) {
		final ByteBuffer buffer = ByteBuffer.wrap(request.toBytes());
		try {
			this.send(buffer, address);
		} catch (NetException e) {
			LOGGER.error("发送UDP消息异常", e);
		}
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
