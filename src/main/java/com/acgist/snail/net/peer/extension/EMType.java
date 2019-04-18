package com.acgist.snail.net.peer.extension;

/**
 * 消息类型
 */
public class EMType {

	public enum Type {
		ut_pex,
		ut_metadata;
	}

	public static final EMType newInstance(Type type, byte value) {
		return new EMType(type, value);
	}
	
	private EMType(Type type, byte value) {
		this.type = type;
		this.value = value;
	}

	private final Type type; // 消息类型
	private final byte value; // 消息值

	public Type getType() {
		return type;
	}

	public byte getValue() {
		return value;
	}

}
