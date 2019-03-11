package com.acgist.snail.pojo.message;

import com.acgist.snail.utils.JsonUtils;

/**
 * 客户端消息
 */
public class ClientMessage {

//	private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessage.class);
	
	/**
	 * 消息类型
	 */
	public enum Type {

		text, // 文本
		close, // 关闭
		notify, // 唤醒
		response; // 响应

	}

	/**
	 * 类型
	 */
	private Type type;
	/**
	 * 消息内容
	 */
	private String body;

	
	public ClientMessage() {
	}
	
	public ClientMessage(Type type) {
		this.type = type;
	}

	public ClientMessage(Type type, String body) {
		this.type = type;
		this.body = body;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	/**
	 * 转换为JSON字符串
	 */
	public String toJson() {
		return JsonUtils.toJson(this);
	}
	
	/**
	 * JSON字符串变成ClientMessage对象
	 */
	public static final ClientMessage valueOf(String content) {
		return JsonUtils.toJava(content, ClientMessage.class);
	}
	
	/**
	 * 消息
	 */
	public static final ClientMessage message(Type type) {
		return message(type, null);
	}
	
	/**
	 * 消息
	 */
	public static final ClientMessage message(Type type, String body) {
		ClientMessage message = new ClientMessage(type);
		message.setBody(body);
		return message;
	}
	
	/**
	 * 文本
	 */
	public static final ClientMessage text(String body) {
		return message(Type.text, body);
	}
	
	/**
	 * 响应
	 */
	public static final ClientMessage response(String body) {
		return message(Type.response, body);
	}
	
}
