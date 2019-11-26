package com.acgist.snail.gui;

import java.awt.TrayIcon.MessageType;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.GuiEvent.Type;
import com.acgist.snail.gui.event.impl.AlertEvent;
import com.acgist.snail.gui.event.impl.BuildEvent;
import com.acgist.snail.gui.event.impl.ExitEvent;
import com.acgist.snail.gui.event.impl.HideEvent;
import com.acgist.snail.gui.event.impl.NoticeEvent;
import com.acgist.snail.gui.event.impl.RefreshTaskListEvent;
import com.acgist.snail.gui.event.impl.RefreshTaskStatusEvent;
import com.acgist.snail.gui.event.impl.ShowEvent;
import com.acgist.snail.gui.event.impl.TorrentEvent;
import com.acgist.snail.net.IMessageHandler;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

import javafx.scene.control.Alert.AlertType;

/**
 * <p>GUI接口</p>
 * 
 * TODO：Loading
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class GuiHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GuiHandler.class);
	
	private static final GuiHandler INSTANCE = new GuiHandler();
	
	/**
	 * 提示窗口类型
	 */
	public enum SnailAlertType {
		
		/** 普通 */
		NONE,
		/** 提示 */
		INFO,
		/** 警告 */
		WARN,
		/** 确认 */
		CONFIRM,
		/** 错误 */
		ERROR;
		
		/**
		 * @return JavaFX提示类型
		 */
		public final AlertType getAlertType() {
			switch (this) {
			case NONE:
				return AlertType.NONE;
			case INFO:
				return AlertType.INFORMATION;
			case WARN:
				return AlertType.WARNING;
			case CONFIRM:
				return AlertType.CONFIRMATION;
			case ERROR:
				return AlertType.ERROR;
			default:
				return AlertType.INFORMATION;
			}
		}
		
	}
	
	/**
	 * 提示消息类型
	 */
	public enum SnailNoticeType {
		
		/** 普通 */
		NONE,
		/** 提示 */
		INFO,
		/** 警告 */
		WARN,
		/** 错误 */
		ERROR;
		
		/**
		 * @return JavaFX消息类型
		 */
		public final MessageType getMessageType() {
			switch (this) {
			case NONE:
				return MessageType.NONE;
			case INFO:
				return MessageType.INFO;
			case WARN:
				return MessageType.WARNING;
			case ERROR:
				return MessageType.ERROR;
			default:
				return MessageType.INFO;
			}
		}
		
	}
	
	/**
	 * <p>事件列表</p>
	 * <p>key=事件类型；value=事件；</p>
	 */
	private static final Map<GuiEvent.Type, GuiEvent> EVENTS = new HashMap<>(GuiEvent.Type.values().length);
	/**
	 * 本地GUI
	 */
	public static final String MODE_GUI = "gui";
	/**
	 * 后台模式
	 */
	public static final String MODE_DAEMO = "daemo";
	/**
	 * 阻塞锁时间：365天
	 */
	private static final int LOCK_DAYS = 365;
	
	static {
		register(BuildEvent.getInstance());
		register(ShowEvent.getInstance());
		register(HideEvent.getInstance());
		register(ExitEvent.getInstance());
		register(AlertEvent.getInstance());
		register(NoticeEvent.getInstance());
		register(TorrentEvent.getInstance());
		register(RefreshTaskListEvent.getInstance());
		register(RefreshTaskStatusEvent.getInstance());
	}
	
	/**
	 * 本地GUI：JavaFX
	 */
	private boolean gui = true;
	/**
	 * 阻塞锁：使用扩展GUI时阻止程序关闭
	 */
	private final Object lock = new Object();
	/**
	 * 扩展GUI消息代理
	 */
	private IMessageHandler extendGuiMessageHandler;
	
	private GuiHandler() {
	}
	
	public static final GuiHandler getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 注册事件
	 */
	public static final void register(GuiEvent event) {
		LOGGER.debug("注册GUI事件：{}-{}", event.type(), event.name());
		EVENTS.put(event.type(), event);
	}

	/**
	 * @param type 事件类型
	 * 
	 * @return 事件
	 */
	private static final GuiEvent handler(GuiEvent.Type type) {
		return EVENTS.get(type);
	}
	
	/**
	 * 初始化GUI
	 */
	public GuiHandler init(String ... args) {
		if(args == null || args.length < 1) {
			this.gui = true;
		} else {
			final String arg = args[0];
			this.gui = !MODE_DAEMO.equalsIgnoreCase(arg);
		}
		LOGGER.info("运行模式：{}", this.gui ? "本地GUI" : "后台模式");
		return this;
	}
	
	/**
	 * 显示窗口
	 */
	public GuiHandler show() {
		this.event(Type.SHOW);
		return this;
	}
	
	/**
	 * 隐藏窗口
	 */
	public GuiHandler hide() {
		this.event(Type.HIDE);
		return this;
	}
	
	/**
	 * 退出窗口
	 */
	public GuiHandler exit() {
		this.event(Type.EXIT);
		return this;
	}

	/**
	 * 创建窗口
	 */
	public GuiHandler build() {
		event(Type.BUILD);
		return this;
	}
	
	/**
	 * 提示窗口
	 * 
	 * @param title 标题
	 * @param message 内容
	 */
	public GuiHandler alert(String title, String message) {
		this.alert(title, message, SnailAlertType.INFO);
		return this;
	}

	/**
	 * 提示窗口
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 */
	public GuiHandler alert(String title, String message, SnailAlertType type) {
		this.event(Type.ALERT, title, message, type);
		return this;
	}
	
	/**
	 * 提示消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 */
	public GuiHandler notice(String title, String message) {
		this.notice(title, message, SnailNoticeType.INFO);
		return this;
	}
	
	/**
	 * 提示消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 */
	public GuiHandler notice(String title, String message, SnailNoticeType type) {
		this.event(Type.NOTICE, title, message, type);
		return this;
	}
	
	/**
	 * 种子文件选择
	 */
	public GuiHandler torrent(ITaskSession taskSession) {
		event(Type.TORRENT, taskSession);
		return this;
	}
	
	/**
	 * 刷新任务列表
	 */
	public GuiHandler refreshTaskList() {
		event(Type.REFRESH_TASK_LIST);
		return this;
	}
	
	/**
	 * 刷新任务状态
	 */
	public GuiHandler refreshTaskStatus() {
		event(Type.REFRESH_TASK_STATUS);
		return this;
	}

	/**
	 * 执行事件
	 * 
	 * @param type 类型
	 * @param args 参数
	 */
	public GuiHandler event(GuiEvent.Type type, Object ... args) {
		if(type == null) {
			return this;
		}
		final GuiEvent event = handler(type);
		if(event == null) {
			LOGGER.warn("未知GUI事件：{}", type);
			return this;
		}
		event.execute(this.gui, args);
		return this;
	}
	
	/**
	 * 注册扩展GUI消息代理
	 */
	public boolean extendGuiMessageHandler(IMessageHandler extendGuiMessageHandler) {
		if(this.gui) {
			LOGGER.debug("已经启用本地GUI：忽略注册扩展GUI消息代理");
			return false;
		} else {
			LOGGER.debug("注册扩展GUI消息代理");
			this.extendGuiMessageHandler = extendGuiMessageHandler;
			return true;
		}
	}
	
	/**
	 * 发送扩展GUI消息
	 */
	public void sendExtendGuiMessage(ApplicationMessage message) {
		if(this.extendGuiMessageHandler != null && message != null) {
			try {
				this.extendGuiMessageHandler.send(message.toString());
			} catch (NetException e) {
				LOGGER.error("发送扩展GUI消息异常", e);
			}
		}
	}
	
	/**
	 * 阻塞锁：防止系统自动关闭
	 */
	public void lock() {
		synchronized (this.lock) {
			ThreadUtils.wait(this.lock, Duration.ofDays(LOCK_DAYS));
		}
	}
	
	/**
	 * 唤醒阻塞锁
	 */
	public void unlock() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}
	
}
