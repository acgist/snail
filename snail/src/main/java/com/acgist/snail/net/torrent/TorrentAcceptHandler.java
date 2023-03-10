package com.acgist.snail.net.torrent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.StunConfig;
import com.acgist.snail.net.UdpAcceptHandler;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.stun.StunMessageHandler;
import com.acgist.snail.net.torrent.dht.DhtMessageHandler;
import com.acgist.snail.net.torrent.utp.UtpContext;

/**
 * <p>Torrent（UTP、DHT、STUN）消息接收代理</p>
 * 
 * @author acgist
 */
public final class TorrentAcceptHandler extends UdpAcceptHandler {
	
	private static final TorrentAcceptHandler INSTANCE = new TorrentAcceptHandler();
	
	public static final TorrentAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>UTP上下文</p>
	 */
	private final UtpContext utpContext = UtpContext.getInstance();
	/**
	 * <p>DHT消息代理</p>
	 */
	private final DhtMessageHandler dhtMessageHandler = new DhtMessageHandler();
	/**
	 * <p>STUN消息代理</p>
	 */
	private final StunMessageHandler stunMessageHandler = new StunMessageHandler();
	
	private TorrentAcceptHandler() {
	}
	
	@Override
	public void handle(DatagramChannel channel) {
		this.utpContext.handle(channel);
		this.dhtMessageHandler.handle(channel);
		this.stunMessageHandler.handle(channel);
	}
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		final byte header = buffer.get(0);
		if(DhtConfig.DHT_HEADER == header) {
			// DHT消息
			return this.dhtMessageHandler;
		}
		if(StunConfig.STUN_HEADER_SEND == header || StunConfig.STUN_HEADER_RECV == header) {
			// STUN消息
			final int magicCookie = buffer.getInt(4);
			if(magicCookie == StunConfig.MAGIC_COOKIE) {
				// 由于UTP数据（DATA）消息也是0x01所以需要验证MAGIC_COOKIE
				return this.stunMessageHandler;
			}
		}
		// UTP消息
		final short connectionId = buffer.getShort(2);
		return this.utpContext.get(connectionId, socketAddress);
	}
	
}
