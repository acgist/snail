package com.acgist.snail.net.upnp;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.utils.NetUtils;

/**
 * Upnp Server
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpServer extends UdpServer<UpnpAcceptHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpServer.class);
	
	private static final int UPNP_TTL = 3;
	// UPNP端口
	public static final int UPNP_PORT = 1900;
	// UPNP地址
	public static final String UPNP_HOST = "239.255.255.250";
	
	private UpnpServer() {
		super(NetUtils.buildUdpChannel(UPNP_PORT, true), "UPNP Server", UpnpAcceptHandler.getInstance());
		this.join(UPNP_TTL, UPNP_HOST);
		this.handler();
	}
	
	private static final UpnpServer INSTANCE = new UpnpServer();
	
	public static final UpnpServer getInstance() {
		return INSTANCE;
	}
	
}
