package com.acgist.snail.system.config;

/**
 * <p>STUN配置</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class StunConfig {

	/**
	 * <p>头部信息长度：{@value}</p>
	 */
	public static final int STUN_HEADER_LENGTH = 20;
	/**
	 * <p>属性头部信息长度：{@value}</p>
	 */
	public static final int ATTRIBUTE_HEADER_LENGTH = 4;
	/**
	 * <p>默认端口：{@value}</p>
	 */
	public static final int DEFAULT_PORT = 3478;
	/**
	 * <p>固定值：{@value}</p>
	 */
	public static final int MAGIC_COOKIE = 0x2112A442;
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
	
	/**
	 * <p>方法类型</p>
	 */
	public enum MethodType {
		
		/** 绑定：请求/响应、指示 */
		BINDING((short) 0x01);
		
		public static final short VALUE_MASK = 0b0000_0000_0000_0001;
		
		/**
		 * <p>方法ID</p>
		 */
		private final short id;
		
		private MethodType(short id) {
			this.id = id;
		}
		
		public short id() {
			return this.id;
		}
		
	}
	
	/**
	 * <p>消息类型</p>
	 */
	public enum MessageType {
		
		/** 请求：服务器会响应 */
		REQUEST(			(byte) 0b00),
		/** 指示：服务器不响应 */
		INDICATION(			(byte) 0b01),
		/** 响应：成功 */
		SUCCESS_RESPONSE(	(byte) 0b10),
		/** 响应：失败 */
		ERROR_RESPONSE(		(byte) 0b11);
		
		/**
		 * <p>C1：{@value}</p>
		 */
		public static final short C1_MASK = 0B0000_0001_0000_0000;
		/**
		 * <p>C0：{@value}</p>
		 */
		public static final short C0_MASK = 0B0000_0000_0001_0000;
		/**
		 * <p>前两位必须为零：{@value}</p>
		 */
		public static final short TYPE_MASK = 0B0011_1111_1111_1111;
		
		/**
		 * <p>消息ID</p>
		 */
		private final byte id;
		
		private MessageType(byte id) {
			this.id = id;
		}
		
		/**
		 * <p>计算对应方法的MessageType值</p>
		 * 
		 * @param methodType 方法类型
		 * 
		 * @return 方法类型标识
		 */
		public short type(MethodType methodType) {
			return (short) (
			(
				((this.id << 7) & C1_MASK) |
				((this.id << 4) & C0_MASK) |
				methodType.id
			) & TYPE_MASK);
		}
		
		public static final MessageType valueOf(short value) {
			final byte id = (byte) (
			(
				((value & C1_MASK) >> 7) |
				((value & C0_MASK) >> 4)
			) & 0xFF);
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
	 * <p>0x0000：保留</p>
	 */
	public enum AttributeType {
		
		//================强制理解：0x0000-0x7FFF================//
		/** 端口映射：明文 */
		MAPPED_ADDRESS(		(short) 0x0001),
		RESPONSE_ADDRESS(	(short) 0x0002),
		CHANGE_ADDRESS(		(short) 0x0003),
		SOURCE_ADDRESS(		(short) 0x0004),
		CHANGED_ADDRESS(	(short) 0x0005),
		USERNAME(			(short) 0x0006),
		PASSWORD(			(short) 0x0007),
		MESSAGE_INTEGRITY(	(short) 0x0008),
		/** 错误：错误响应时使用 */
		ERROR_CODE(			(short) 0x0009),
		UNKNOWN_ATTRIBUTES(	(short) 0x000A),
		REFLECTED_FROM(		(short) 0x000B),
		REALM(				(short) 0x0014),
		NONCE(				(short) 0x0015),
		/** 端口映射：使用异或处理数据 */
		XOR_MAPPED_ADDRESS(	(short) 0x0020),
		//================选择理解：0x8000-0xFFFF================//
		SOFTWARE(			(short) 0x8022),
		ALTERNATE_SERVER(	(short) 0x8023),
		FINGERPRINT(		(short) 0x8028);
		
		/**
		 * <p>属性ID</p>
		 */
		private final short id;
		
		private AttributeType(short id) {
			this.id = id;
		}
		
		public short id() {
			return this.id;
		}

		public static final AttributeType valueOf(short id) {
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
	 * <p>错误编码：300-699</p>
	 */
	public enum ErrorCode {
		
		/** 尝试替换 */
		TRY_ALTERNATE(		300),
		/** 请求错误 */
		BAD_REQUEST(		400),
		/** 没有授权 */
		UNAUTHORIZED(		401),
		/** 未知属性 */
		UNKNOWN_ATTRIBUTE(	420),
		/** NONCE过期 */
		STALE_NONCE(		438),
		/** 服务器错误 */
		SERVER_ERROR(		500);
		
		/**
		 * <p>错误编码</p>
		 */
		private final int code;
		
		private ErrorCode(int code) {
			this.code = code;
		}
		
		public int code() {
			return this.code;
		}
		
	}
	
}
