package com.acgist.snail.net.dht;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.system.config.SystemConfig;

/**
 * DHT服务端
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtServer extends UdpServer<DhtMessageHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtServer.class);
	
	private DhtServer() {
		super(SystemConfig.getServicePort(), "DHT Server", DhtMessageHandler.class);
		this.handler();
	}
	
	private static final DhtServer INSTANCE = new DhtServer();
	
	public static final DhtServer getInstance() {
		return INSTANCE;
	}
	
}
