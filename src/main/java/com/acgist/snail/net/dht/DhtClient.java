package com.acgist.snail.net.dht;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * DHT协议<br>
 * 基本协议：UDP
 */
public class DhtClient extends UdpClient<DhtMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtClient.class);
	
	private final SocketAddress address;
	
	private static final DatagramChannel CHANNEL;
	
	static {
		CHANNEL = NetUtils.buildUdpChannel(SystemConfig.getDhtPort());
		UdpClient.bindServerHandler(new DhtMessageHandler(), CHANNEL);
	}
	
	private DhtClient(SocketAddress address) {
		super("DHT Client", new DhtMessageHandler());
		this.open();
		this.address = address;
	}
	
	public static final DhtClient newInstance(final String host, final int port) {
		return newInstance(new InetSocketAddress(host, port));
	}
	
	public static final DhtClient newInstance(SocketAddress address) {
		return new DhtClient(address);
	}

	/**
	 * 使用和DHT Server一条的通道
	 */
	@Override
	public boolean open() {
		return open(CHANNEL);
	}
	
	public void ping() {
		this.handler.ping(this.address);
	}
	
	public static final void release() {
		LOGGER.info("UDP Client关闭：DHT Client");
		IoUtils.close(CHANNEL);
	}
	
}
