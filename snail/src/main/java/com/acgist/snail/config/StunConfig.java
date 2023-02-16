package com.acgist.snail.config;

/**
 * STUN配置
 * 
 * @author acgist
 */
public final class StunConfig {

	/**
	 * 默认端口
	 */
	public static final int DEFAULT_PORT = 3478;
	/**
	 * STUN固定值
	 */
	public static final int MAGIC_COOKIE = 0x2112A442;
	/**
	 * 头部信息长度
	 */
	public static final int HEADER_STUN_LENGTH = 20;
	/**
	 * 属性头部信息长度
	 */
	public static final int HEADER_ATTRIBUTE_LENGTH = 4;
	/**
	 * TransactionID长度
	 */
	public static final int TRANSACTION_ID_LENGTH = 12;
	/**
	 * IPv4
	 */
	public static final int IPV4 = 0x01;
	/**
	 * IPv6
	 */
	public static final int IPV6 = 0x02;
	/**
	 * STUN消息开头字符：请求、指示
	 */
	public static final byte STUN_HEADER_SEND = 0x00;
	/**
	 * STUN消息开头字符：响应
	 */
	public static final byte STUN_HEADER_RECV = 0x01;
	
	private StunConfig() {
	}
	
	/**
	 * 方法类型
	 * 
	 * @author acgist
	 */
	public enum MethodType {
		
		/**
		 * 绑定：请求、响应、指示
		 * 
		 * @see MessageType#REQUEST
		 * @see MessageType#INDICATION
		 * @see MessageType#RESPONSE_SUCCESS
		 * @see MessageType#RESPONSE_ERROR
		 */
		BINDING((short) 0x01);
		
		/**
		 * 消息MASK
		 */
		public static final short MASK = 0B0000_0000_0000_0001;
		
		/**
		 * 方法ID
		 */
		private final short id;
		
		/**
		 * @param id 方法ID
		 */
		private MethodType(short id) {
			this.id = id;
		}
		
		/**
		 * @return 方法ID
		 */
		public short id() {
			return this.id;
		}
		
	}
	
	/**
	 * 消息类型
	 * 
	 * @author acgist
	 */
	public enum MessageType {
		
		/**
		 * 请求：服务器会响应
		 */
		REQUEST((byte) 0B00),
		/**
		 * 指示：服务器不响应
		 */
		INDICATION((byte) 0B01),
		/**
		 * 响应：成功
		 */
		RESPONSE_SUCCESS((byte) 0B10),
		/**
		 * 响应：失败
		 */
		RESPONSE_ERROR((byte) 0B11);
		
		/**
		 * C0
		 */
		public static final short C0_MASK = 0B0000_0000_0001_0000;
		/**
		 * C1
		 */
		public static final short C1_MASK = 0B0000_0001_0000_0000;
		/**
		 * 前两位必须零
		 */
		public static final short TYPE_MASK = 0B0011_1111_1111_1111;
		
		/**
		 * 消息ID
		 */
		private final byte id;
		
		/**
		 * @param id 消息ID
		 */
		private MessageType(byte id) {
			this.id = id;
		}
		
		/**
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
			) & MessageType.TYPE_MASK);
		}
		
		/**
		 * @param value 消息类型标识
		 * 
		 * @return 方法类型
		 */
		public static final MessageType of(short value) {
			final short type = (short) (value & MessageType.TYPE_MASK);
			final byte id = (byte) (((type & C1_MASK) >> 7) | ((type & C0_MASK) >> 4));
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
	 * 属性类型
	 * 保留：0x0000
	 * 强制解析：0x0000-0x7FFF
	 * 可选解析：0x8000-0xFFFF
	 * 
	 * @author acgist
	 */
	public enum AttributeType {
	    
		MAPPED_ADDRESS((short) 0x0001),
		RESPONSE_ADDRESS((short) 0x0002),
		CHANGE_REQUEST((short) 0x0003),
		SOURCE_ADDRESS((short) 0x0004),
		CHANGED_ADDRESS((short) 0x0005),
		USERNAME((short) 0x0006),
		PASSWORD((short) 0x0007),
		MESSAGE_INTEGRITY((short) 0x0008),
		ERROR_CODE((short) 0x0009),
		UNKNOWN_ATTRIBUTES((short) 0x000A),
		REFLECTED_FROM((short) 0x000B),
		REALM((short) 0x0014),
		NONCE((short) 0x0015),
		XOR_MAPPED_ADDRESS((short) 0x0020),
		SOFTWARE((short) 0x8022),
		ALTERNATE_SERVER((short) 0x8023),
		FINGERPRINT((short) 0x8028);
		
		/**
		 * 属性ID
		 */
		private final short id;
		
		/**
		 * @param id 属性ID
		 */
		private AttributeType(short id) {
			this.id = id;
		}
		
		/**
		 * @return 属性ID
		 */
		public short id() {
			return this.id;
		}

		/**
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
	 * 错误编码
	 * 编码范围：300-699
	 * 
	 * @author acgist
	 */
	public enum ErrorCode {
		
		/**
		 * 尝试替换
		 */
		TRY_ALTERNATE(300),
		/**
		 * 请求错误
		 */
		BAD_REQUEST(400),
		/**
		 * 没有授权
		 */
		UNAUTHORIZED(401),
		/**
		 * 未知属性
		 */
		UNKNOWN_ATTRIBUTE(420),
		/**
		 * NONCE过期
		 */
		STALE_NONCE(438),
		/**
		 * 服务器错误
		 */
		SERVER_ERROR(500);
		
		/**
		 * 错误编码
		 */
		private final int code;
		
		/**
		 * @param code 错误编码
		 */
		private ErrorCode(int code) {
			this.code = code;
		}
		
		/**
		 * @return 错误编码
		 */
		public int code() {
			return this.code;
		}
		
	}
	
}
