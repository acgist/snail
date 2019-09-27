package com.acgist.snail.pojo.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.exception.NetException;

/**
 * Application消息（系统消息）
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ApplicationMessage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessage.class);
	
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
		
		/** GUI注册 */
		gui,
		/** 文本 */
		text,
		/** 关闭连接 */
		close,
		/** 唤醒窗口 */
		notify,
		/** 关闭程序 */
		shutdown,
		/** 新建任务 */
		taskNew,
		/** 任务列表 */
		taskList,
		/** 开始任务 */
		taskStart,
		/** 暂停任务 */
		taskPause,
		/** 删除任务 */
		taskDelete,
		
		// 主动消息
		
		/** 提示窗口 */
		alert,
		/** 提示消息 */
		notice,
		/** 刷新（任务） */
		refresh,
		/** 响应 */
		response;

	}

	/**
	 * 类型
	 */
	private Type type;
	/**
	 * 消息内容
	 */
	private String body;

	private ApplicationMessage() {
	}
	
	private ApplicationMessage(Type type) {
		this.type = type;
	}

	private ApplicationMessage(Type type, String body) {
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
		try (final var encoder = BEncodeEncoder.newInstance()) {
			encoder.newMap()
				.put("type", this.type.name());
			if(this.body != null) {
				encoder.put("body", this.body);
			}
			return encoder.flush().toString();
		} catch (Exception e) {
			LOGGER.error("系统消息B编码异常", e);
		}
		return null;
	}
	
	/**
	 * B编码字符串变成ApplicationMessage对象
	 */
	public static final ApplicationMessage valueOf(String content) {
		try (final var decoder = BEncodeDecoder.newInstance(content.getBytes())) {
			decoder.nextMap();
			if(decoder.isEmpty()) {
				return null;
			}
			final String type = decoder.getString("type");
			final String body = decoder.getString("body");
			return ApplicationMessage.message(Type.valueOf(type), body);
		} catch (NetException e) {
			LOGGER.error("系统消息读取异常", e);
		}
		return null;
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
