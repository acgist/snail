package com.acgist.snail.net.torrent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.stun.StunMessageHandler;
import com.acgist.snail.net.torrent.dht.DhtMessageHandler;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpService;
import com.acgist.snail.system.config.StunConfig;

/**
 * <p>Torrent（UTP、DHT、STUN）消息接收器</p>
 * <p>DHT和STUN消息都使用头一个字符验证，STUN需要进一步验证MagicCookie，其余消息均属于UTP。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class TorrentAcceptHandler extends UdpAcceptHandler {
	
	private static final TorrentAcceptHandler INSTANCE = new TorrentAcceptHandler();
	
	/**
	 * DHT消息开头字符
	 */
	private static final byte DHT_HEADER = 'd';
	/**
	 * STUN消息开头字符：请求、指示
	 */
	private static final byte STUN_HEADER_SEND = 0x00;
	/**
	 * STUN消息开头字符：响应
	 */
	private static final byte STUN_HEADER_RECV = 0x01;
	
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
	/**
	 * STUN消息代理
	 */
	private final StunMessageHandler stunMessageHandler = new StunMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		// 区分类型：DHT、UTP、STUN
		final byte header = buffer.get(0);
		if(DHT_HEADER == header) { // DHT
			return this.dhtMessageHandler;
		} else if(STUN_HEADER_SEND == header || STUN_HEADER_RECV == header) { // STUN
			final int magicCookie = buffer.getInt(4);
			if(magicCookie == StunConfig.MAGIC_COOKIE) {
				return this.stunMessageHandler;
			}
		}
		// UTP
		final short connectionId = buffer.getShort(2); // 连接ID
		return this.utpService.get(connectionId, socketAddress);
	}
	
}
