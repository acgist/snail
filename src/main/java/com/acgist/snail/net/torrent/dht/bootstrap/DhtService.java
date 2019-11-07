package com.acgist.snail.net.torrent.dht.bootstrap;

import com.acgist.snail.system.config.SystemConfig;

/**
 * DHT Service
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DhtService {

//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtService.class);
	
	private static final DhtService INSTANCE = new DhtService();
	
	/**
	 * 请求id最小值
	 */
	private static final int MIN_ID_VALUE = 0;
	/**
	 * 请求id最大值
	 */
	private static final int MAX_ID_VALUE = 2 << 15;

	/**
	 * 请求ID
	 */
	private int requestId = MIN_ID_VALUE;

	private DhtService() {
	}
	
	public static final DhtService getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>生成一个两字节的请求ID</p>
	 * <p>如果大于{@linkplain #MAX_ID_VALUE 最大值}则设置为{@linkplain #MIN_ID_VALUE 最小值}重新开始生成。</p>
	 */
	public byte[] requestId() {
		final byte[] bytes = new byte[2];
		synchronized (this) {
			if(++requestId >= MAX_ID_VALUE) {
				requestId = MIN_ID_VALUE;
			}
			bytes[0] = (byte) (requestId / SystemConfig.UNSIGNED_BYTE_MAX);
			bytes[1] = (byte) (requestId % SystemConfig.UNSIGNED_BYTE_MAX);
		}
		return bytes;
	}
	
}
