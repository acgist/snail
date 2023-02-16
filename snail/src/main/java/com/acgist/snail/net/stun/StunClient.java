package com.acgist.snail.net.stun;

import java.net.InetSocketAddress;

import com.acgist.snail.config.StunConfig;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>Stun客户端</p>
 * <p>注意：简单的STUN客户端（没有实现所有功能）</p>
 * 
 * @author acgist
 */
public final class StunClient extends UdpClient<StunMessageHandler> {
	
	/**
	 * @param socketAddress 地址
	 */
	private StunClient(final InetSocketAddress socketAddress) {
		super("STUN Client", new StunMessageHandler(socketAddress));
	}
	
	/**
	 * <p>新建Stun客户端</p>
	 * 
	 * @param host 服务器地址
	 * 
	 * @return Stun客户端
	 */
	public static final StunClient newInstance(final String host) {
		return newInstance(host, StunConfig.DEFAULT_PORT);
	}
	
	/**
	 * <p>新建Stun客户端</p>
	 * 
	 * @param host 服务器地址
	 * @param port 服务器端口
	 * 
	 * @return Stun客户端
	 */
	public static final StunClient newInstance(final String host, final int port) {
		return newInstance(NetUtils.buildSocketAddress(host, port));
	}
	
	/**
	 * <p>新建Stun客户端</p>
	 * 
	 * @param socketAddress 服务器地址
	 * 
	 * @return Stun客户端
	 */
	public static final StunClient newInstance(final InetSocketAddress socketAddress) {
		return new StunClient(socketAddress);
	}

	@Override
	public boolean open() {
		return this.open(TorrentServer.getInstance().channel());
	}
	
	/**
	 * <p>发送映射消息</p>
	 */
	public void mapping() {
		this.handler.changeRequest();
	}
	
}
