package com.acgist.snail.net.torrent.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.dht.DhtMessageHandler;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpService;

/**
 * UDP服务消息处理：UTP、DHT
 * 
 * @author acgist
 * @since 1.1.0
 */
public class TorrentAcceptHandler extends UdpAcceptHandler {
	
	/**
	 * DHT消息开头字符
	 */
	private static final byte DHT_HEADER = 'd';
	
	private static final TorrentAcceptHandler INSTANCE = new TorrentAcceptHandler();
	
	private TorrentAcceptHandler() {
	}
	
	public static final TorrentAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	/**
	 * UTP Service
	 */
	private final UtpService utpService = UtpService.getInstance();
	/**
	 * DHT消息代理
	 */
	private final DhtMessageHandler dhtMessageHandler = new DhtMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		final byte header = buffer.get(0); // 类型：区分DHT和UTP消息
		if(DHT_HEADER == header) { // DHT
			return this.dhtMessageHandler;
		} else { // UTP
			final short connectionId = buffer.getShort(2); // 连接ID
			return this.utpService.get(connectionId, socketAddress);
		}
	}
	
}
