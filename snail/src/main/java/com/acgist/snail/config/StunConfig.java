package com.acgist.snail.config;

/**
 * <p>STUN配置</p>
 * 
 * @author acgist
 */
public final class StunConfig {

	/**
	 * <p>默认端口：{@value}</p>
	 */
	public static final int DEFAULT_PORT = 3478;
	/**
	 * <p>固定值：{@value}</p>
	 */
	public static final int MAGIC_COOKIE = 0x2112A442;
	/**
	 * <p>头部信息长度：{@value}</p>
	 */
	public static final int HEADER_LENGTH_STUN = 20;
	/**
	 * <p>属性头部信息长度：{@value}</p>
	 */
	public static final int HEADER_LENGTH_ATTRIBUTE = 4;
	/**
	 * <p>TransactionID长度：{@value}</p>
	 */
	public static final int TRANSACTION_ID_LENGTH = 12;
	/**
	 * <p>IPv4：{@value}</p>
	 */
	public static final int IPV4 = 0x01;
	/**
	 * <p>IPv6：{@value}</p>
	 */
	public static final int IPV6 = 0x02;
	
	private StunConfig() {
	}
	
	/**
	 * <p>方法类型</p>
	 * 
	 * @author acgist
	 */
	public enum MethodType {
		
		/**
		 * <p>绑定：请求、响应、指示</p>
		 * 
		 * @see MessageType#REQUEST
		 * @see MessageType#INDICATION
		 * @see MessageType#RESPONSE_SUCCESS
		 * @see MessageType#RESPONSE_ERROR
		 */
		BINDING((short) 0x01);
		
		/**
		 * <p>消息MASK：{@value}</p>
		 */
		public static final short MASK = 0B0000_0000_0000_0001;
		
		/**
		 * <p>方法ID</p>
		 */
		private final short id;
		
		/**
		 * @param id 方法ID
		 */
		private MethodType(short id) {
			this.id = id;
		}
		
		/**
		 * <p>获取方法ID</p>
		 * 
		 * @return 方法ID
		 */
		public short id() {
			return this.id;
		}
		
	}
	
	/**
	 * <p>消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum MessageType {
		
		/**
		 * <p>请求：服务器会响应</p>
		 */
		REQUEST((byte) 0B00),
		/**
		 * <p>指示：服务器不响应</p>
		 */
		INDICATION((byte) 0B01),
		/**
		 * <p>响应：成功</p>
		 */
		RESPONSE_SUCCESS((byte) 0B10),
		/**
		 * <p>响应：失败</p>
		 */
		RESPONSE_ERROR((byte) 0B11);
		
		/**
		 * <p>C0：{@value}</p>
		 */
		public static final short C0_MASK = 0B0000_0000_0001_0000;
		/**
		 * <p>C1：{@value}</p>
		 */
		public static final short C1_MASK = 0B0000_0001_0000_0000;
		/**
		 * <p>前两位必须零：{@value}</p>
		 */
		public static final short TYPE_MASK = 0B0011_1111_1111_1111;
		
		/**
		 * <p>消息ID</p>
		 */
		private final byte id;
		
		/**
		 * @param id 消息ID
		 */
		private MessageType(byte id) {
			this.id = id;
		}
		
		/**
		 * <p>通过方法类型获取消息类型标识</p>
		 * 
		 * @param methodType 方法类型
		 * 
		 * @return 消息类型标识
		 */
		public short of(MethodType methodType) {
			return (short) (
			(
				((this.id << 7) & C1_MASK) |
				((this.id << 4) & C0_MASK) |
				methodType.id
			) & TYPE_MASK);
		}
		
		/**
		 * <p>通过消息类型标识获取方法类型</p>
		 * 
		 * @param value 消息类型标识
		 * 
		 * @return 方法类型
		 */
		public static final MessageType of(short value) {
			final byte id = (byte) (((value & C1_MASK) >> 7) | ((value & C0_MASK) >> 4));
			final var types = MessageType.values();
			for (MessageType messageType : types) {
				if(messageType.id == id) {
					return messageType;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * <p>属性类型</p>
	 * <p>保留：0x0000</p>
	 * <p>强制解析：0x0000-0x7FFF</p>
	 * <p>可选解析：0x8000-0xFFFF</p>
	 * 
	 * @author acgist
	 */
	public enum AttributeType {
		
		/**
		 * <p>端口映射：明文</p>
		 */
		MAPPED_ADDRESS((short) 0x0001),
		RESPONSE_ADDRESS((short) 0x0002),
		CHANGE_ADDRESS((short) 0x0003),
		SOURCE_ADDRESS((short) 0x0004),
		CHANGED_ADDRESS((short) 0x0005),
		USERNAME((short) 0x0006),
		PASSWORD((short) 0x0007),
		MESSAGE_INTEGRITY((short) 0x0008),
		/**
		 * <p>错误：错误响应</p>
		 */
		ERROR_CODE((short) 0x0009),
		UNKNOWN_ATTRIBUTES((short) 0x000A),
		REFLECTED_FROM((short) 0x000B),
		REALM((short) 0x0014),
		NONCE((short) 0x0015),
		/**
		 * <p>端口映射：异或处理数据</p>
		 */
		XOR_MAPPED_ADDRESS((short) 0x0020),
		SOFTWARE((short) 0x8022),
		ALTERNATE_SERVER((short) 0x8023),
		FINGERPRINT((short) 0x8028);
		
		/**
		 * <p>属性ID</p>
		 */
		private final short id;
		
		/**
		 * @param id 属性ID
		 */
		private AttributeType(short id) {
			this.id = id;
		}
		
		/**
		 * <p>获取属性ID</p>
		 * 
		 * @return 属性ID
		 */
		public short id() {
			return this.id;
		}

		/**
		 * <p>通过属性ID获取属性类型</p>
		 * 
		 * @param id 属性ID
		 * 
		 * @return 属性类型
		 */
		public static final AttributeType of(short id) {
			final var types = AttributeType.values();
			for (AttributeType attributeType : types) {
				if(attributeType.id == id) {
					return attributeType;
				}
			}
			return null;
		}
		
	}

	/**
	 * <p>错误编码</p>
	 * <p>编码范围：300-699</p>
	 * 
	 * @author acgist
	 */
	public enum ErrorCode {
		
		/**
		 * <p>尝试替换</p>
		 */
		TRY_ALTERNATE(300),
		/**
		 * <p>请求错误</p>
		 */
		BAD_REQUEST(400),
		/**
		 * <p>没有授权</p>
		 */
		UNAUTHORIZED(401),
		/**
		 * <p>未知属性</p>
		 */
		UNKNOWN_ATTRIBUTE(420),
		/**
		 * <p>NONCE过期</p>
		 */
		STALE_NONCE(438),
		/**
		 * <p>服务器错误</p>
		 */
		SERVER_ERROR(500);
		
		/**
		 * <p>错误编码</p>
		 */
		private final int code;
		
		/**
		 * @param code 错误编码
		 */
		private ErrorCode(int code) {
			this.code = code;
		}
		
		/**
		 * <p>获取错误编码</p>
		 * 
		 * @return 错误编码
		 */
		public int code() {
			return this.code;
		}
		
	}
	
}
