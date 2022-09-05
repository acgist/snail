package com.acgist.snail.config;

import com.acgist.snail.utils.EnumUtils;

/**
 * <p>UTP配置</p>
 * 
 * @author acgist
 */
public final class UtpConfig {

	/**
	 * <p>消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>数据</p>
		 */
		DATA((byte) 0),
		/**
		 * <p>结束</p>
		 */
		FIN((byte) 1),
		/**
		 * <p>响应</p>
		 */
		STATE((byte) 2),
		/**
		 * <p>重置</p>
		 */
		RESET((byte) 3),
		/**
		 * <p>握手</p>
		 */
		SYN((byte) 4);
		
		/**
		 * <p>类型</p>
		 */
		private final byte type;
		/**
		 * <p>版本类型：类型 + 版本</p>
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
		 * <p>获取类型</p>
		 * 
		 * @return 类型
		 */
		public byte type() {
			return this.type;
		}
		
		/**
		 * <p>获取版本类型</p>
		 * 
		 * @return 版本类型
		 */
		public byte typeVersion() {
			return this.typeVersion;
		}
		
		/**
		 * <p>通过版本类型获取消息类型</p>
		 * 
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
	 * <p>UTP版本：{@value}</p>
	 */
	public static final byte VERSION = 1;
	/**
	 * <p>UTP消息请求头默认长度：{@value}</p>
	 */
	public static final int HEADER_LENGTH = 20;
	/**
	 * <p>UTP消息请求头最小长度：{@value}</p>
	 */
	public static final int HEADER_MIN_LENGTH = 20;
	/**
	 * <p>UTP扩展：{@value}</p>
	 */
	public static final byte EXTENSION = 0;
	/**
	 * <p>UTP扩展消息最小长度：{@value}</p>
	 */
	public static final int EXTENSION_MIN_LENGTH = 2;
	/**
	 * <p>UTP最大包长度：{@value}</p>
	 * <p>UDP最大包长度：1500 - 20（IP头） - 8（UDP头） = 1472</p>
	 * <p>UTP最大包长度：1472 - 20（UTP扩展消息头） = 1452</p>
	 * 
	 * @see #EXTENSION
	 * @see #HEADER_MIN_LENGTH
	 */
	public static final int PACKET_MAX_LENGTH = 1452;
	/**
	 * <p>默认窗口大小：{@value}</p>
	 */
	public static final int WND_SIZE = SystemConfig.ONE_MB;
	/**
	 * <p>最大发送次数：{@value}</p>
	 */
	public static final byte MAX_PUSH_TIMES = 3;
	/**
	 * <p>快速重传发送ACK次数：{@value}</p>
	 */
	public static final byte FAST_ACK_RETRY_TIMES = 3;
	
	private UtpConfig() {
	}
	
}
