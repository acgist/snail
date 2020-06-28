package com.acgist.snail.net.upnp;

import com.acgist.snail.net.UdpServer;

/**
 * <p>UPNP服务端</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class UpnpServer extends UdpServer<UpnpAcceptHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpServer.class);
	
	private static final UpnpServer INSTANCE = new UpnpServer();
	
	/**
	 * <p>TTL</p>
	 */
	private static final int UPNP_TTL = 2;
	/**
	 * <p>UPNP端口</p>
	 */
	public static final int UPNP_PORT = 1900;
	/**
	 * <p>UPNP地址</p>
	 */
	public static final String UPNP_HOST = "239.255.255.250";
	/**
	 * <p>UPNP根设备</p>
	 */
	public static final String UPNP_ROOT_DEVICE = "upnp:rootdevice";
	
	private UpnpServer() {
		// 不监听UPNP端口：防止收到很多其他应用消息
		super("UPNP Server", UpnpAcceptHandler.getInstance());
		this.join(UPNP_TTL, UPNP_HOST);
		this.handle();
	}
	
	public static final UpnpServer getInstance() {
		return INSTANCE;
	}
	
}
