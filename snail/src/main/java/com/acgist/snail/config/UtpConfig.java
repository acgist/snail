package com.acgist.snail.config;

/**
 * <p>UTP配置</p>
 * 
 * @author acgist
 */
public final class UtpConfig {

	/**
	 * <p>UTP消息类型</p>
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
		 * <p>帧类型</p>
		 */
		private final byte type;
		/**
		 * <p>消息类型：帧类型 + 版本</p>
		 * 
		 * @see #type
		 * @see UtpConfig#UTP_VERSION
		 */
		private final byte typeVersion;
		
		/**
		 * @param type 帧类型
		 */
		private Type(byte type) {
			this.type = type;
			this.typeVersion = (byte) ((type << 4) | (UTP_VERSION & 0xFF));
		}
		
		/**
		 * <p>获取帧类型</p>
		 * 
		 * @return 帧类型
		 */
		public byte type() {
			return this.type;
		}
		
		/**
		 * <p>获取消息类型</p>
		 * 
		 * @return 消息类型
		 */
		public byte typeVersion() {
			return this.typeVersion;
		}
		
		/**
		 * <p>通过消息类型获取UTP消息类型</p>
		 * 
		 * @param typeVersion 消息类型
		 * 
		 * @return UTP消息类型
		 */
		public static final Type of(byte typeVersion) {
			// 获取帧类型
			final byte value = (byte) (typeVersion >> 4);
			// 使用switch效率更高：DATA类型数据最多基本没有影响
			final Type[] types = Type.values();
			for (Type type : types) {
				if(type.type == value) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * <p>版本：{@value}</p>
	 */
	public static final byte UTP_VERSION = 1;
	/**
	 * <p>扩展：{@value}</p>
	 */
	public static final byte EXTENSION = 0;
	/**
	 * <p>UTP最大包长度：{@value}</p>
	 * <p>UDP最大包长度：1500 - 20（IP头） - 8（UDP头） = 1472</p>
	 * <p>UTP最大包长度：1472 - 20（UTP扩展消息头） = 1452</p>
	 * <p>UTP扩展消息头长度：20（没有扩展的情况下）</p>
	 * 
	 * @see #EXTENSION
	 */
	public static final int UTP_PACKET_MAX_LENGTH = 1452;
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
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private UtpConfig() {
	}
	
}
