package com.acgist.snail.config;

import com.acgist.snail.utils.EnumUtils;

/**
 * 快传配置
 * 
 * @author acgist
 */
public final class QuickConfig {

	/**
	 * 协议标记
	 * 用来区分多种协议
	 */
	public static final byte QUICK_HEADER = 'q';
	/**
	 * 文件包长度
	 * UDP最大包长度：1500 - 20（IP头） - 8（UDP头） = 1472
	 */
	public static final int PACKET_MAX_LENGTH = 1472;
	/**
	 * 头部长度
	 */
	public static final int HEADER_LENGTH = 8;
	/**
	 * 消息方向：请求
	 */
	public static final byte DIRECTION_RES = 0;
	/**
	 * 消息方向：响应
	 */
	public static final byte DIRECTION_ACK = 1;
	/**
	 * 初始化信号量
	 * 影响文件发送速度：发送过程中会自动调节
	 */
	public static final int DEFAULT_SEMAPHORE = 16;
	/**
	 * 快速重传次数
	 * 如果一个序号多次确认表示丢包，启动快速重传功能。
	 */
	public static final byte RETRY_TIMES = 3;
	/**
	 * 重传包的数量：丢包
	 */
	public static final int RETRY_PACK_SIZE_LOSS = 2;
	/**
	 * 重传包的数量：超时
	 */
	public static final int RETRY_PACK_SIZE_TIMEOUT = 8;
	
	private QuickConfig() {
	}
	
	/**
	 * 消息类型
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * 关闭
		 */
		CLOSE((byte) 0),
		/**
		 * 连接
		 */
		CONNECT((byte) 1),
		/**
		 * 协商
		 */
		NEGOTIATE((byte) 2),
		/**
		 * 传输
		 */
		TRANSPORT((byte) 3);

		/**
		 * 类型
		 */
		private final byte value;
		
		/**
		 * 索引数据
		 */
		private static final Type[] INDEX = EnumUtils.index(Type.class, Type::value);
		
		private Type(byte value) {
			this.value = value;
		}
		
		/**
		 * @return 类型
		 */
		public byte value() {
			return this.value;
		}
		
		/**
		 * @param direction 方向
		 * 
		 * @return 方向类型
		 */
		public byte directionType(byte direction) {
			return (byte)(direction << 4 | this.value);
		}
		
		/**
		 * @param directionType 方向类型
		 * 
		 * @return 类型
		 */
		public static final Type of(byte directionType) {
			// 计算类型
			final byte value = (byte) (directionType & 0B00001111);
			// 使用索引
			if(value < 0 || value >= INDEX.length) {
				return null;
			}
			return INDEX[value];
		}
		
	}
	
}
