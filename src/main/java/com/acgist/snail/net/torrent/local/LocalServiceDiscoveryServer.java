package com.acgist.snail.net.torrent.local;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.utils.NetUtils;

/**
 * 本地发现服务端
 * 
 * @author acgist
 * @since 1.0.0
 */
public class LocalServiceDiscoveryServer extends UdpServer<LocalServiceDiscoveryAcceptHandler> {
	
	// TTL
	private static final int LSD_TTL = 1;
	// 本地发现端口
	public static final int LSD_PORT = 6771;
	// 本地发现IPv4地址
	public static final String LSD_HOST = "239.192.152.143";
	// 本地发现IPv6地址
	public static final String LSD_HOST_IPv6 = "[ff15::efc0:988f]";
	
	public LocalServiceDiscoveryServer() {
		super(NetUtils.buildUdpChannel(LSD_PORT, true), "LSD Server", LocalServiceDiscoveryAcceptHandler.getInstance());
		this.join(LSD_TTL, LSD_HOST);
		this.handler();
	}
	
	private static final LocalServiceDiscoveryServer INSTANCE = new LocalServiceDiscoveryServer();
	
	public static final LocalServiceDiscoveryServer getInstance() {
		return INSTANCE;
	}

}
