package com.acgist.snail.pojo.message;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;

/**
 * Application消息（系统消息）
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ApplicationMessage {
	
	/**
	 * 失败
	 */
	public static final String FAIL = "fail";
	/**
	 * 成功
	 */
	public static final String SUCCESS = "success";

	/**
	 * 消息类型
	 */
	public enum Type {

		// 被动消息
		gui, // 外部GUI注册
		text, // 文本
		close, // 关闭连接
		notify, // 唤醒窗口
		shutdown, // 关闭程序
		taskNew, // 新建任务
		taskList, // 任务列表
		taskStart, // 开始任务
		taskPause, // 暂停任务
		taskDelete, // 删除任务
		
		// 主动消息
		alert, // 提示窗口
		notice, // 提示消息
		refresh, // 刷新（任务）
		
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
	 * 转换为B编码字符串
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
	 * B编码字符串变成ApplicationMessage对象
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
