package com.acgist.snail.net.dht;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.dht.bootstrap.DhtService;

/**
 * DHT协议<br>
 * 基本协议：UDP
 */
public class DhtClient extends UdpClient<DhtMessageHandler> {

	private DhtClient() {
		super("DHT Client", new DhtMessageHandler());
		this.open();
		this.handle();
	}
	
	private static final DhtClient INSTANCE = new DhtClient();
	
	public static final DhtClient getInstance() {
		return INSTANCE;
	}

	/**
	 * 使用和DHT Server一条的通道
	 */
	@Override
	public boolean open() {
		return open(DhtService.getInstance().dhtChannel());
	}
	
}
