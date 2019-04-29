package com.acgist.snail.net.dht.bootstrap;

public class DhtService {

//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtService.class);
	
	private static final int MIN_VALUE = 0;
	private static final int MAX_VALUE = 2 << 15;
	private static final int UNSIGNED_BYTE_MAX = 2 << 7;

	private int id = MIN_VALUE;
	
	private static final DhtService INSTANCE = new DhtService();

	private DhtService() {
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
	
}
