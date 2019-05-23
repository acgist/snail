package com.acgist.snail.net.bootstrap;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * UDP服务：UTP、DHT
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UdpServiceServer extends UdpServer<UdpServiceAcceptHandler> {

	private UdpServiceServer() {
		super(SystemConfig.getServicePort(), "UTP-DHT Server", UdpServiceAcceptHandler.getInstance());
		this.handler();
	}
	
	private static final UdpServiceServer INSTANCE = new UdpServiceServer();
	
	public static final UdpServiceServer getInstance() {
		return INSTANCE;
	}

}
