package com.acgist.snail.net.torrent.dht.bootstrap;

/**
 * DHT Service
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtService {

//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtService.class);
	
	private static final int MIN_VALUE = 0;
	private static final int MAX_VALUE = 2 << 15;
	private static final int UNSIGNED_BYTE_MAX = 2 << 7;

	private int requestId = MIN_VALUE;
	
	private static final DhtService INSTANCE = new DhtService();

	private DhtService() {
	}
	
	public static final DhtService getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>生成一个两字节的请求ID</p>
	 * <p>如果大于最大值{@linkplain #MAX_VALUE}则设置为最小值{@linkplain #MIN_VALUE}重新开始生成。</p>
	 */
	public byte[] requestId() {
		final byte[] bytes = new byte[2];
		synchronized (this) {
			if(++requestId >= MAX_VALUE) {
				requestId = MIN_VALUE;
			}
			bytes[0] = (byte) (requestId / UNSIGNED_BYTE_MAX);
			bytes[1] = (byte) (requestId % UNSIGNED_BYTE_MAX);
		}
		return bytes;
	}
	
}
