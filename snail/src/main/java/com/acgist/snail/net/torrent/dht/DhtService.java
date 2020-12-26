package com.acgist.snail.net.torrent.dht;

/**
 * <p>DHT Service</p>
 * 
 * @author acgist
 */
public final class DhtService {

//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtService.class);
	
	private static final DhtService INSTANCE = new DhtService();
	
	public static final DhtService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>消息ID最小值：{@value}</p>
	 */
	private static final int MIN_ID_VALUE = 0;
	/**
	 * <p>消息ID最大值：{@value}</p>
	 */
	private static final int MAX_ID_VALUE = 2 << 15;

	/**
	 * <p>消息ID</p>
	 */
	private int requestId = MIN_ID_VALUE;

	private DhtService() {
	}

	/**
	 * <p>生成一个两字节的消息ID</p>
	 * <p>如果大于{@linkplain #MAX_ID_VALUE 最大值}则设置为{@linkplain #MIN_ID_VALUE 最小值}重新开始生成</p>
	 * 
	 * @return 消息ID
	 */
	public byte[] buildRequestId() {
		final byte[] bytes = new byte[2];
		synchronized (this) {
			if(++this.requestId >= MAX_ID_VALUE) {
				this.requestId = MIN_ID_VALUE;
			}
			// 位运算
			bytes[0] = (byte) ((this.requestId >> 8) & 0xFF);
			bytes[1] = (byte) (this.requestId & 0xFF);
			// 常规运算
//			bytes[0] = (byte) (this.requestId / SystemConfig.UNSIGNED_BYTE_MAX);
//			bytes[1] = (byte) (this.requestId % SystemConfig.UNSIGNED_BYTE_MAX);
		}
		return bytes;
	}
	
}
