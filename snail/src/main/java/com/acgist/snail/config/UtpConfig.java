package com.acgist.snail.config;

import com.acgist.snail.utils.EnumUtils;

/**
 * UTP配置
 * 
 * @author acgist
 */
public final class UtpConfig {

	/**
	 * 消息类型
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * 数据
		 */
		DATA((byte) 0),
		/**
		 * 结束
		 */
		FIN((byte) 1),
		/**
		 * 响应
		 */
		STATE((byte) 2),
		/**
		 * 重置
		 */
		RESET((byte) 3),
		/**
		 * 握手
		 */
		SYN((byte) 4);
		
		/**
		 * 类型
		 */
		private final byte type;
		/**
		 * 版本类型：类型 + 版本
		 * 
		 * @see #type
		 * @see UtpConfig#VERSION
		 */
		private final byte typeVersion;
		
		/**
		 * 索引数据
		 */
		private static final Type[] INDEX = EnumUtils.index(Type.class, Type::type);
		
		/**
		 * @param type 类型
		 */
		private Type(byte type) {
			this.type = type;
			this.typeVersion = (byte) (type << 4 | VERSION);
		}
		
		/**
		 * @return 类型
		 */
		public byte type() {
			return this.type;
		}
		
		/**
		 * @return 版本类型
		 */
		public byte typeVersion() {
			return this.typeVersion;
		}
		
		/**
		 * @param typeVersion 版本类型
		 * 
		 * @return 消息类型
		 */
		public static final Type of(byte typeVersion) {
			// 获取类型
			final byte value = (byte) (typeVersion >> 4);
			// 使用索引
			if(value < 0 || value >= INDEX.length) {
				return null;
			}
			return INDEX[value];
			// 使用switch
//			return switch (value) {
//			case 0x00 -> DATA;
//			case 0x01 -> FIN;
//			case 0x02 -> STATE;
//			case 0x03 -> RESET;
//			case 0x04 -> SYN;
//			default -> null;
//			};
			// 使用for
//			final Type[] types = Type.values();
//			for (Type type : types) {
//				if(type.type == value) {
//					return type;
//				}
//			}
//			return null;
		}
		
	}
	
	/**
	 * UTP版本：{@value}
	 */
	public static final byte VERSION = 1;
	/**
	 * UTP消息请求头默认长度：{@value}
	 */
	public static final int HEADER_LENGTH = 20;
	/**
	 * UTP消息请求头最小长度：{@value}
	 */
	public static final int HEADER_MIN_LENGTH = 20;
	/**
	 * UTP扩展：{@value}
	 */
	public static final byte EXTENSION = 0;
	/**
	 * UTP扩展消息最小长度：{@value}
	 */
	public static final int EXTENSION_MIN_LENGTH = 2;
	/**
	 * UTP最大包长度：{@value}
	 * UDP最大包长度：1500 - 20（IP头） - 8（UDP头） = 1472
	 * UTP最大包长度：1472 - 20（UTP扩展消息头） = 1452
	 * 
	 * @see #EXTENSION
	 * @see #HEADER_MIN_LENGTH
	 */
	public static final int PACKET_MAX_LENGTH = 1452;
	/**
	 * 默认窗口大小：{@value}
	 */
	public static final int WND_SIZE = SystemConfig.ONE_MB;
	/**
	 * 最大发送次数：{@value}
	 */
	public static final byte MAX_PUSH_TIMES = 3;
	/**
	 * 快速重传发送ACK次数：{@value}
	 */
	public static final byte FAST_ACK_RETRY_TIMES = 3;
	
	private UtpConfig() {
	}
	
}
