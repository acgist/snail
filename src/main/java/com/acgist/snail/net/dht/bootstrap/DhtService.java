package com.acgist.snail.net.dht.bootstrap;

import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

public class DhtService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtService.class);
	
	private static final int MIN_VALUE = 0;
	private static final int MAX_VALUE = 2 << 15;
	private static final int UNSIGNED_BYTE_MAX = 2 << 7;

	private int id = MIN_VALUE;
	private final DatagramChannel dhtChannel;
	
	private static final DhtService INSTANCE = new DhtService();

	private DhtService() {
		dhtChannel = NetUtils.buildUdpChannel(SystemConfig.getDhtPort());
	}
	
	public static final DhtService getInstance() {
		return INSTANCE;
	}

	/**
	 * 生成一个两字节的ID
	 */
	public String id() {
		final byte[] bytes = new byte[2];
		synchronized (this) {
			if(++id >= MAX_VALUE) {
				id = MIN_VALUE;
			}
			bytes[0] = (byte) (id / UNSIGNED_BYTE_MAX);
			bytes[1] = (byte) (id % UNSIGNED_BYTE_MAX);
		}
		return new String(bytes);
	}

	/**
	 * DHT通道
	 */
	public DatagramChannel dhtChannel() {
		return this.dhtChannel;
	}
	
	public void close() {
		LOGGER.info("关闭DHT通道");
		IoUtils.close(this.dhtChannel);
	}
	
}
