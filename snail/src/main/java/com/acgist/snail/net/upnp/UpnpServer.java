package com.acgist.snail.net.upnp;

import java.net.StandardProtocolFamily;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UPNP服务端</p>
 * 
 * @author acgist
 */
public final class UpnpServer extends UdpServer<UpnpAcceptHandler> {

	private static final UpnpServer INSTANCE = new UpnpServer();
	
	public static final UpnpServer getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>UPNP组播端口</p>
	 */
	public static final int UPNP_PORT = 1900;
	/**
	 * <p>UPNP组播地址（IPv4）</p>
	 */
	public static final String UPNP_HOST = "239.255.255.250";
	/**
	 * <p>UPNP组播地址（IPv6）</p>
	 */
	public static final String UPNP_HOST_IPV6 = "[ff15::efff:fffa]";
	/**
	 * <p>UPNP根设备</p>
	 */
	public static final String UPNP_ROOT_DEVICE = "upnp:rootdevice";
	
	private UpnpServer() {
		// 不监听UPNP端口：防止收到很多其他应用消息
		super("UPNP Server", UpnpAcceptHandler.getInstance());
		this.join(UDP_TTL, upnpHost());
		this.handle();
	}

	/**
	 * <p>获取UPNP组播地址</p>
	 * 
	 * @return UPNP组播地址
	 */
	public static final String upnpHost() {
		if(NetUtils.LOCAL_PROTOCOL_FAMILY == StandardProtocolFamily.INET) {
			return UPNP_HOST;
		} else {
			return UPNP_HOST_IPV6;
		}
	}
	
}
