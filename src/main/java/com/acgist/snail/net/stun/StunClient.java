package com.acgist.snail.net.stun;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.system.config.StunConfig;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>Stun客户端</p>
 * 
 * <p>注：简单的STUN客户端（没有实现所有功能）</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class StunClient extends UdpClient<StunMessageHandler> {
	
	public StunClient(InetSocketAddress socketAddress) {
		super("STUN Client", new StunMessageHandler(), socketAddress);
	}
	
	public static final StunClient newInstance(final String host) {
		return newInstance(NetUtils.buildSocketAddress(host, StunConfig.DEFAULT_PORT));
	}
	
	public static final StunClient newInstance(final String host, final int port) {
		return newInstance(NetUtils.buildSocketAddress(host, port));
	}
	
	public static final StunClient newInstance(InetSocketAddress socketAddress) {
		return new StunClient(socketAddress);
	}

	@Override
	public boolean open() {
		return open(TorrentServer.getInstance().channel());
	}

	/**
	 * <p>发送映射消息</p>
	 */
	public void mappedAddress() {
		this.handler.mappedAddress();
	}
	
}
