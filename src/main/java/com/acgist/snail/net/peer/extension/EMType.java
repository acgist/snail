package com.acgist.snail.net.peer.extension;

/**
 * 消息类型
 */
public class EMType {

	public enum Type {
		ut_pex,
		ut_metadata;
	}

	public static final EMType newInstance(Type type, Integer value) {
		return new EMType(type, value);
	}
	
	private EMType(Type type, Integer value) {
		this.type = type;
		this.value = value;
	}

	private final Type type; // 消息类型
	private final Integer value; // 消息值

	public Type getType() {
		return type;
	}

	public Integer getValue() {
		return value;
	}

}
