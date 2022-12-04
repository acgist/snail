package com.acgist.snail.gui;

import java.util.EnumMap;
import java.util.Map;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.IContext;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.GuiEvent.Type;
import com.acgist.snail.gui.event.adapter.AlertEventAdapter;
import com.acgist.snail.gui.event.adapter.BuildEventAdapter;
import com.acgist.snail.gui.event.adapter.ExitEventAdapter;
import com.acgist.snail.gui.event.adapter.HideEventAdapter;
import com.acgist.snail.gui.event.adapter.MultifileEventAdapter;
import com.acgist.snail.gui.event.adapter.NoticeEventAdapter;
import com.acgist.snail.gui.event.adapter.RefreshTaskListEventAdapter;
import com.acgist.snail.gui.event.adapter.RefreshTaskStatusEventAdapter;
import com.acgist.snail.gui.event.adapter.ResponseEventAdapter;
import com.acgist.snail.gui.event.adapter.ShowEventAdapter;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.IMessageSender;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.application.ApplicationMessage;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * GUI上下文
 * 
 * @author acgist
 */
public final class GuiContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GuiContext.class);
	
	private static final GuiContext INSTANCE = new GuiContext();
	
	public static final GuiContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 运行模式
	 * 
	 * @author acgist
	 */
	public enum Mode {
		
		/**
		 * 本地模式：本地GUI（JavaFX）
		 */
		NATIVE,
		/**
		 * 后台模式：扩展GUI
		 * 通过系统消息和系统通知来完成系统管理和任务管理
		 * 
		 * @see ApplicationMessage.Type
		 */
		EXTEND;

		/**
		 * @param mode 运行模式
		 * 
		 * @return 运行模式
		 */
		public static final Mode of(String mode) {
			final var values = Mode.values();
			for (Mode value : values) {
				if(value.name().equalsIgnoreCase(mode)) {
					return value;
				}
			}
			return Mode.NATIVE;
		}
		
	}
	
	/**
	 * 消息类型
	 * 
	 * @author acgist
	 */
	public enum MessageType {
		
		/**
		 * 普通
		 */
		NONE,
		/**
		 * 提示
		 */
		INFO,
		/**
		 * 警告
		 */
		WARN,
		/**
		 * 错误
		 */
		ERROR,
		/**
		 * 确认
		 */
		CONFIRM;
		
		/**
		 * @param value 消息类型
		 * 
		 * @return 消息类型
		 */
		public static final MessageType of(String value) {
			final var types = MessageType.values();
			for (MessageType type : types) {
				if(type.name().equalsIgnoreCase(value)) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * 启动参数：运行模式
	 */
	private static final String ARGS_MODE = "mode";
	
	/**
	 * 运行模式
	 */
	private Mode mode = Mode.NATIVE;
	/**
	 * 扩展GUI阻塞锁（阻止程序关闭）
	 */
	private final Object lock;
	/**
	 * 扩展GUI消息代理
	 */
	private IMessageSender extendGuiMessageSender;
	/**
	 * 事件Map
	 * 事件类型=事件
	 */
	private final Map<GuiEvent.Type, GuiEvent> eventMapping;
	
	private GuiContext() {
		this.lock = new Object();
		this.eventMapping = new EnumMap<>(GuiEvent.Type.class);
	}
	
	/**
	 * 注册GUI事件
	 * 
	 * @param event GUI事件
	 */
	public static final void register(GuiEvent event) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("注册GUI事件：{}-{}", event.type(), event.name());
		}
		INSTANCE.eventMapping.put(event.type(), event);
	}

	/**
	 * 注册GUI事件默认适配器
	 */
	public static final void registerAdapter() {
		GuiContext.register(new ShowEventAdapter());
		GuiContext.register(new HideEventAdapter());
		GuiContext.register(new ExitEventAdapter());
		GuiContext.register(new BuildEventAdapter());
		GuiContext.register(new AlertEventAdapter());
		GuiContext.register(new NoticeEventAdapter());
		GuiContext.register(new ResponseEventAdapter());
		GuiContext.register(new MultifileEventAdapter());
		GuiContext.register(new RefreshTaskListEventAdapter());
		GuiContext.register(new RefreshTaskStatusEventAdapter());
	}
	
	/**
	 * 初始化GUI上下文
	 * 
	 * @param args 启动参数
	 * 
	 * @return GuiContext
	 */
	public GuiContext init(String ... args) {
		if(ArrayUtils.isEmpty(args)) {
			LOGGER.debug("没有设置启动参数");
		} else {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info("启动参数：{}", SymbolConfig.Symbol.COMMA.join(args));
			}
			final Map<String, String> argsMap = StringUtils.argsMap(args);
			// 运行模式
			final String mode = argsMap.get(ARGS_MODE);
			this.mode = Mode.of(mode);
			LOGGER.debug("运行模式：{}", this.mode);
		}
		return this;
	}
	
	/**
	 * 显示窗口
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#SHOW
	 * @see ShowEventAdapter
	 */
	public GuiContext show() {
		return this.event(Type.SHOW);
	}
	
	/**
	 * 隐藏窗口
	 * 
	 * @return GuiContext
	 * 
	 * @see Type@HIDE
	 * @see HideEventAdapter
	 */
	public GuiContext hide() {
		return this.event(Type.HIDE);
	}
	
	/**
	 * 退出窗口
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#EXIT
	 * @see ExitEventAdapter
	 */
	public GuiContext exit() {
		return this.event(Type.EXIT);
	}

	/**
	 * 新建窗口
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#BUILD
	 * @see BuildEventAdapter
	 */
	public GuiContext build() {
		return this.event(Type.BUILD);
	}
	
	/**
	 * 窗口消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#ALERT
	 * @see MessageType#INFO
	 * @see AlertEventAdapter
	 */
	public GuiContext alert(String title, String message) {
		return this.alert(title, message, GuiContext.MessageType.INFO);
	}

	/**
	 * 窗口消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#ALERT
	 * @see AlertEventAdapter
	 */
	public GuiContext alert(String title, String message, GuiContext.MessageType type) {
		return this.event(Type.ALERT, title, message, type);
	}
	
	/**
	 * 提示消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#NOTICE
	 * @see MessageType#INFO
	 * @see NoticeEventAdapter
	 */
	public GuiContext notice(String title, String message) {
		return this.notice(title, message, GuiContext.MessageType.INFO);
	}
	
	/**
	 * 提示消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#NOTICE
	 * @see NoticeEventAdapter
	 */
	public GuiContext notice(String title, String message, GuiContext.MessageType type) {
		return this.event(Type.NOTICE, title, message, type);
	}
	
	/**
	 * 选择下载文件
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#MULTIFILE
	 */
	public GuiContext multifile(ITaskSession taskSession) {
		return this.event(Type.MULTIFILE, taskSession);
	}
	
	/**
	 * 刷新任务列表
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#REFRESH_TASK_LIST
	 */
	public GuiContext refreshTaskList() {
		return this.event(Type.REFRESH_TASK_LIST);
	}
	
	/**
	 * 刷新任务状态
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#REFRESH_TASK_STATUS
	 */
	public GuiContext refreshTaskStatus() {
		return this.event(Type.REFRESH_TASK_STATUS);
	}

	/**
	 * 响应消息
	 * 
	 * @param message 消息
	 * 
	 * @return GuiContext
	 * 
	 * @see Type#RESPONSE
	 */
	public GuiContext response(String message) {
		return this.event(Type.RESPONSE, message);
	}
	
	/**
	 * 执行事件
	 * 
	 * @param type 类型
	 * @param args 参数
	 * 
	 * @return GuiContext
	 */
	public GuiContext event(GuiEvent.Type type, Object ... args) {
		if(type == null) {
			LOGGER.warn("错误GUI事件：{}", type);
			return this;
		}
		final GuiEvent event = this.eventMapping.get(type);
		if(event == null) {
			LOGGER.warn("未知GUI事件：{}", type);
			return this;
		}
		LOGGER.debug("执行GUI事件：{}", type);
		event.execute(this.mode, args);
		return this;
	}
	
	/**
	 * 注册扩展GUI消息代理
	 * 
	 * @param extendGuiMessageSender 扩展GUI消息代理
	 * 
	 * @return 是否注册成功
	 */
	public boolean extendGuiMessageHandler(IMessageSender extendGuiMessageSender) {
		if(this.mode == Mode.NATIVE) {
			LOGGER.debug("忽略注册扩展GUI消息代理：已经启用本地GUI");
			return false;
		} else {
			LOGGER.debug("注册扩展GUI消息代理");
			this.extendGuiMessageSender = extendGuiMessageSender;
			return true;
		}
	}
	
	/**
	 * 发送扩展GUI消息
	 * 
	 * @param message 扩展GUI消息
	 */
	public void sendExtendGuiMessage(ApplicationMessage message) {
		if(message == null) {
			LOGGER.warn("扩展GUI消息错误：{}", message);
			return;
		}
		if(this.extendGuiMessageSender != null) {
			try {
				this.extendGuiMessageSender.send(message.toString());
			} catch (NetException e) {
				LOGGER.error("发送扩展GUI消息异常", e);
			}
		} else {
			LOGGER.warn("扩展GUI消息代理没有注册");
		}
	}
	
	/**
	 * 添加扩展GUI阻塞锁
	 */
	public void lock() {
		synchronized (this.lock) {
			try {
				this.lock.wait(Long.MAX_VALUE);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.debug("线程等待异常", e);
			}
		}
	}
	
	/**
	 * 释放扩展GUI阻塞锁
	 */
	public void unlock() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}

}
