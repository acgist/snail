package com.acgist.snail.net.torrent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.config.StunConfig;
import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.stun.StunMessageHandler;
import com.acgist.snail.net.torrent.dht.DhtMessageHandler;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpService;

/**
 * <p>Torrent（UTP、DHT、STUN）消息接收器</p>
 * <p>DHT和STUN消息都使用头一个字符验证：STUN需要进一步验证MagicCookie</p>
 * <p>如果不是DHT和STUN消息则属于UTP消息</p>
 * 
 * @author acgist
 */
public final class TorrentAcceptHandler extends UdpAcceptHandler {
	
	private static final TorrentAcceptHandler INSTANCE = new TorrentAcceptHandler();
	
	public static final TorrentAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>DHT消息开头字符</p>
	 */
	private static final byte DHT_HEADER = 'd';
	/**
	 * <p>STUN消息开头字符：请求、指示</p>
	 */
	private static final byte STUN_HEADER_SEND = 0x00;
	/**
	 * <p>STUN消息开头字符：响应</p>
	 */
	private static final byte STUN_HEADER_RECV = 0x01;
	
	private TorrentAcceptHandler() {
	}
	
	/**
	 * <p>UTP Service</p>
	 */
	private final UtpService utpService = UtpService.getInstance();
	/**
	 * <p>DHT消息代理</p>
	 */
	private final DhtMessageHandler dhtMessageHandler = new DhtMessageHandler();
	/**
	 * <p>STUN消息代理</p>
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
			// 验证：MAGIC_COOKIE
			if(magicCookie == StunConfig.MAGIC_COOKIE) {
				return this.stunMessageHandler;
			}
		}
		// UTP
		final short connectionId = buffer.getShort(2); // 连接ID
		return this.utpService.get(connectionId, socketAddress);
	}
	
}
