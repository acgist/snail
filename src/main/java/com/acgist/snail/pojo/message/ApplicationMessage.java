package com.acgist.snail.pojo.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Application消息</p>
 * <p>通过系统消息可以实现系统管理和任务管理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ApplicationMessage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessage.class);
	
	/**
	 * <p>失败：{@value}</p>
	 */
	public static final String FAIL = "fail";
	/**
	 * <p>成功：{@value}</p>
	 */
	public static final String SUCCESS = "success";

	/**
	 * <p>系统消息类型</p>
	 */
	public enum Type {
		
		//================被动消息================//
		
		/** GUI注册 */
		GUI,
		/** 文本消息 */
		TEXT,
		/** 关闭连接 */
		CLOSE,
		/** 唤醒窗口 */
		NOTIFY,
		/** 关闭程序 */
		SHUTDOWN,
		/** 新建任务 */
		TASK_NEW,
		/** 任务列表 */
		TASK_LIST,
		/** 开始任务 */
		TASK_START,
		/** 暂停任务 */
		TASK_PAUSE,
		/** 删除任务 */
		TASK_DELETE,
		
		//================主动消息================//
		
		/** 提示窗口 */
		ALERT,
		/** 提示消息 */
		NOTICE,
		/** 刷新任务 */
		REFRESH,
		/** 响应消息 */
		RESPONSE;

		/**
		 * <p>消息类型转换（忽略大小写）</p>
		 * 
		 * @param name 类型名称
		 * 
		 * @return 消息类型
		 */
		public static final Type valueOfName(String name) {
			final var types = Type.values();
			for (Type type : types) {
				if(type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
			return null;
		}
		
	}

	/**
	 * <p>消息类型</p>
	 */
	private Type type;
	/**
	 * <p>消息内容</p>
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
	
	/**
	 * <p>读取系统文本消息（B编码）</p>
	 * 
	 * @param content 系统文本消息
	 * 
	 * @return 系统消息
	 */
	public static final ApplicationMessage valueOf(String content) {
		try {
			final var decoder = BEncodeDecoder.newInstance(content.getBytes());
			decoder.nextMap();
			if(decoder.isEmpty()) {
				return null;
			}
			final String type = decoder.getString("type");
			final String body = decoder.getString("body");
			final Type messageType = Type.valueOfName(type);
			if(messageType == null) {
				return null;
			}
			return ApplicationMessage.message(messageType, body);
		} catch (NetException e) {
			LOGGER.error("读取系统消息异常：{}", content, e);
		}
		return null;
	}
	
	/**
	 * <p>消息</p>
	 * 
	 * @param type 消息类型
	 * 
	 * @return 系统消息
	 */
	public static final ApplicationMessage message(Type type) {
		return message(type, null);
	}
	
	/**
	 * <p>消息</p>
	 * 
	 * @param type 消息类型
	 * @param body 消息内容
	 * 
	 * @return 系统消息
	 */
	public static final ApplicationMessage message(Type type, String body) {
		return new ApplicationMessage(type, body);
	}
	
	/**
	 * <p>文本</p>
	 * 
	 * @param body 消息内容
	 * 
	 * @return 系统消息
	 */
	public static final ApplicationMessage text(String body) {
		return message(Type.TEXT, body);
	}
	
	/**
	 * <p>响应</p>
	 * 
	 * @param body 消息内容
	 * 
	 * @return 系统消息
	 */
	public static final ApplicationMessage response(String body) {
		return message(Type.RESPONSE, body);
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
	 * <p>转为系统文本消息（B编码）</p>
	 * 
	 * @return 系统文本消息
	 */
	@Override
	public String toString() {
		final var encoder = BEncodeEncoder.newInstance();
		encoder.newMap().put("type", this.type.name());
		if(this.body != null) {
			encoder.put("body", this.body);
		}
		return encoder.flush().toString();
	}
	
}
