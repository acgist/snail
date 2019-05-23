package com.acgist.snail.net.bootstrap;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.dht.DhtMessageHandler;

/**
 * UDP服务消息处理：UTP、DHT
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UdpServiceAcceptHandler extends UdpAcceptHandler {
	
	/**
	 * DHT开头字符串
	 */
	private static final byte DHT_HEADER = 'd';
	
	private static final UdpServiceAcceptHandler INSTANCE = new UdpServiceAcceptHandler();
	
	private UdpServiceAcceptHandler() {
	}
	
	public static final UdpServiceAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	private DhtMessageHandler dhtMessageHandler = new DhtMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress address) {
		buffer.flip();
		final byte header = buffer.get();
		buffer.position(buffer.limit()).limit(buffer.capacity());
		if(DHT_HEADER == header) {
			return dhtMessageHandler;
		} else {
			return null; // TODO：utp
		}
	}
	
}
