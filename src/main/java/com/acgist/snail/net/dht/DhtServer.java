package com.acgist.snail.net.dht;

import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * DHT服务端
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtServer.class);
	
	private DatagramChannel channel;
	
	private DhtServer() {
		channel = NetUtils.buildUdpChannel(SystemConfig.getServicePort());
		UdpClient.bindServerHandler(new DhtMessageHandler(), channel);
	}
	
	private static final DhtServer INSTANCE = new DhtServer();
	
	public static final DhtServer getInstance() {
		return INSTANCE;
	}
	
	public DatagramChannel channel() {
		return channel;
	}
	
	public void shutdown() {
		LOGGER.info("DHT Server关闭");
		IoUtils.close(channel);
	}

}
