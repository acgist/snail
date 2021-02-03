package com.acgist.snail.pojo.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;

/**
 * <p>Application消息</p>
 * <p>通过系统消息可以实现系统管理和任务管理</p>
 * 
 * @author acgist
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
	 * <p>系统消息、系统通知类型</p>
	 * <p>系统消息（被动消息）：系统接收来自GUI通知</p>
	 * <p>系统通知（主动消息）：系统主动发送GUI通知</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>系统消息：GUI注册</p>
		 */
		GUI,
		/**
		 * <p>系统消息：文本消息</p>
		 */
		TEXT,
		/**
		 * <p>系统消息：关闭连接</p>
		 */
		CLOSE,
		/**
		 * <p>系统消息：唤醒窗口</p>
		 */
		NOTIFY,
		/**
		 * <p>系统消息：关闭程序</p>
		 */
		SHUTDOWN,
		/**
		 * <p>系统消息：新建任务</p>
		 */
		TASK_NEW,
		/**
		 * <p>系统消息：任务列表</p>
		 */
		TASK_LIST,
		/**
		 * <p>系统消息：开始任务</p>
		 */
		TASK_START,
		/**
		 * <p>系统消息：暂停任务</p>
		 */
		TASK_PAUSE,
		/**
		 * <p>系统消息：删除任务</p>
		 */
		TASK_DELETE,
		/**
		 * <p>系统通知：显示窗口</p>
		 */
		SHOW,
		/**
		 * <p>系统通知：隐藏窗口</p>
		 */
		HIDE,
		/**
		 * <p>系统通知：窗口消息</p>
		 */
		ALERT,
		/**
		 * <p>系统通知：提示消息</p>
		 */
		NOTICE,
		/**
		 * <p>系统通知：刷新任务列表</p>
		 */
		REFRESH_TASK_LIST,
		/**
		 * <p>系统通知：刷新任务状态</p>
		 */
		REFRESH_TASK_STATUS,
		/**
		 * <p>系统通知：响应消息</p>
		 */
		RESPONSE;

		/**
		 * <p>消息类型转换（忽略大小写）</p>
		 * 
		 * @param name 类型名称
		 * 
		 * @return 消息类型
		 */
		public static final Type of(String name) {
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

	/**
	 * @param type 消息类型
	 * @param body 消息内容
	 */
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
			final Type messageType = Type.of(type);
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
	
	/**
	 * <p>获取消息类型</p>
	 * 
	 * @return 消息类型
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * <p>设置消息类型</p>
	 * 
	 * @param type 消息类型
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * <p>获取消息内容</p>
	 * 
	 * @return 消息内容
	 */
	public String getBody() {
		return this.body;
	}

	/**
	 * <p>设置消息内容</p>
	 * 
	 * @param body 消息内容
	 */
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
