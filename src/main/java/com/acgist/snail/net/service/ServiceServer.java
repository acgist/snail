package com.acgist.snail.net.service;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * UDP服务：UTP、DHT
 * 
 * @author acgist
 * @since 1.1.0
 */
public class ServiceServer extends UdpServer<ServiceAcceptHandler> {

	private ServiceServer() {
		super(SystemConfig.getServicePort(), "Service(UTP/DHT) Server", ServiceAcceptHandler.getInstance());
		this.handler();
	}
	
	private static final ServiceServer INSTANCE = new ServiceServer();
	
	public static final ServiceServer getInstance() {
		return INSTANCE;
	}

}
