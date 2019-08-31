package com.acgist.snail.pojo.message;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;

/**
 * Application消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ApplicationMessage {

	/**
	 * 消息类型
	 */
	public enum Type {

		text, // 文本
		close, // 关闭Socket连接
		notify, // 唤醒窗口
		shutdown, // 关闭程序
		tasknew, // 新建任务
		tasklist, // 任务列表
		taskdelete, // 删除任务
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

	
	public ApplicationMessage() {
	}
	
	public ApplicationMessage(Type type) {
		this.type = type;
	}

	public ApplicationMessage(Type type, String body) {
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
	@Override
	public String toString() {
		final BEncodeEncoder encoder = BEncodeEncoder.newInstance().newMap();
		encoder.put("type", this.type.name());
		if(this.body != null) {
			encoder.put("body", this.body);
		}
		return encoder.flush().toString();
	}
	
	/**
	 * JSON字符串变成ApplicationMessage对象
	 */
	public static final ApplicationMessage valueOf(String content) {
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(content.getBytes());
		decoder.nextMap();
		final String type = decoder.getString("type");
		final String body = decoder.getString("body");
		return new ApplicationMessage(Type.valueOf(type), body);
	}
	
	/**
	 * 消息
	 */
	public static final ApplicationMessage message(Type type) {
		return message(type, null);
	}
	
	/**
	 * 消息
	 */
	public static final ApplicationMessage message(Type type, String body) {
		return new ApplicationMessage(type, body);
	}
	
	/**
	 * 文本
	 */
	public static final ApplicationMessage text(String body) {
		return message(Type.text, body);
	}
	
	/**
	 * 响应
	 */
	public static final ApplicationMessage response(String body) {
		return message(Type.response, body);
	}
	
}
