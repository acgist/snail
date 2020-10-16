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
		
		/** 数据 */
		DATA((byte) 0),
		/** 结束 */
		FIN((byte) 1),
		/** 响应 */
		STATE((byte) 2),
		/** 重置 */
		RESET((byte) 3),
		/** 握手 */
		SYN((byte) 4);
		
		/**
		 * <p>帧类型</p>
		 */
		private final byte type;
		/**
		 * <p>消息类型：帧类型 + 版本</p>
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
			final byte value = (byte) (typeVersion >> 4); // 获取帧类型
			// 使用switch效率是否更高
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
	 * <p>扩展</p>
	 */
	public static final byte EXTENSION = 0;
	/**
	 * <p>UTP最大包长度：{@value}</p>
	 * <p>UDP最大包长度：1500 - 20(IP头) - 8(UDP头) = 1472</p>
	 * <p>UTP最大包长度：1472 - 20(UTP扩展消息头) = 1452</p>
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
