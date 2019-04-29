package com.acgist.snail.net.dht;

import com.acgist.snail.net.UdpServer;
import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.system.config.SystemConfig;

public class DhtServer extends UdpServer {

	private DhtServer() {
		super("DHT Server");
	}

	private static final DhtServer INSTANCE = new DhtServer();
	
	public static final DhtServer getInstance() {
		return INSTANCE;
	}
	
	@Override
	public boolean listen() {
		return this.listen(null, SystemConfig.getDhtPort());
	}

	/**
	 * 使用和DHT Client一条的通道
	 */
	@Override
	public boolean listen(String host, int port) {
		return this.listen(DhtService.getInstance().dhtChannel(), DhtMessageHandler.class);
	}

}
