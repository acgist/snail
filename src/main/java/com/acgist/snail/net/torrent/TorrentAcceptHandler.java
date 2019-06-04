package com.acgist.snail.net.torrent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.dht.DhtMessageHandler;
import com.acgist.snail.net.utp.bootstrap.UtpService;

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
	
	private final UtpService utpService = UtpService.getInstance();
	
	private TorrentAcceptHandler() {
	}
	
	public static final TorrentAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	private DhtMessageHandler dhtMessageHandler = new DhtMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		buffer.flip();
		final byte header = buffer.get();
		if(DHT_HEADER == header) {
			buffer.position(buffer.limit()).limit(buffer.capacity());
			return dhtMessageHandler;
		} else {
			final short connectId = buffer.getShort(2); // 连接ID
			buffer.position(buffer.limit()).limit(buffer.capacity());
			return utpService.get(connectId, socketAddress);
		}
	}

}
