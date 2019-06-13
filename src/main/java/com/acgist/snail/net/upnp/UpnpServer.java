package com.acgist.snail.net.upnp;

import com.acgist.snail.net.UdpServer;

/**
 * Upnp Server
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpServer extends UdpServer<UpnpAcceptHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpServer.class);
	
	private UpnpServer() {
		super(-1, "UPNP Server", UpnpAcceptHandler.getInstance());
		this.handler();
	}
	
	private static final UpnpServer INSTANCE = new UpnpServer();
	
	public static final UpnpServer getInstance() {
		return INSTANCE;
	}
	
}
