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
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

import javafx.scene.control.Alert.AlertType;

/**
 * <p>GUI接口</p>
 * <p>启动参数：args[0]=gui：本地GUI</p>
 * <p>启动参数：args[0]=daemo：后台模式</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class GuiHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GuiHandler.class);
	
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
	
	/**
	 * 阻塞锁：防止程序关闭
	 */
	private final Object lock = new Object();
	
	/**
	 * 外部GUI消息代理
	 */
	private IMessageHandler messageHandler;
	
	/**
	 * alert提示窗口类型
	 */
	public enum SnailAlertType {
		
		/** 普通 */
		none,
		/** 提示 */
		info,
		/** 警告 */
		warn,
		/** 确认 */
		confirm,
		/** 错误 */
		error;
		
		public AlertType getAlertType() {
			switch (this) {
			case none:
				return AlertType.NONE;
			case info:
				return AlertType.INFORMATION;
			case warn:
				return AlertType.WARNING;
			case confirm:
				return AlertType.CONFIRMATION;
			case error:
				return AlertType.ERROR;
			default:
				return AlertType.INFORMATION;
			}
		}
		
	}
	
	/**
	 * notice提示消息类型
	 */
	public enum SnailNoticeType {
		
		/** 普通 */
		none, // 
		/** 提示 */
		info,
		/** 警告 */
		warn,
		/** 错误 */
		error;
		
		public MessageType getMessageType() {
			switch (this) {
			case none:
				return MessageType.NONE;
			case info:
				return MessageType.INFO;
			case warn:
				return MessageType.WARNING;
			case error:
				return MessageType.ERROR;
			default:
				return MessageType.INFO;
			}
		}
		
	}
	
	/**
	 * 本地GUI：JavaFX
	 */
	private boolean gui = true;
	
	private static final GuiHandler INSTANCE = new GuiHandler();
	
	/**
	 * 事件列表
	 */
	private static final Map<GuiEvent.Type, GuiEvent> EVENTS = new HashMap<>(20);
	
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
	
	private GuiHandler() {
	}
	
	public static final GuiHandler getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 注册事件
	 */
	public static final void register(GuiEvent event) {
		LOGGER.debug("注册事件：{}-{}", event.type(), event.name());
		EVENTS.put(event.type(), event);
	}

	/**
	 * 获取事件
	 */
	private static final GuiEvent handler(GuiEvent.Type type) {
		return EVENTS.get(type);
	}
	
	/**
	 * 初始化
	 */
	public GuiHandler init(String ... args) {
		if(args == null || args.length < 1) {
			this.gui = true;
		} else {
			final String arg = args[0];
			this.gui = !MODE_DAEMO.equalsIgnoreCase(arg);
		}
		LOGGER.debug("运行模式：{}", this.gui ? "本地GUI" : "后台模式");
		return this;
	}
	
	/**
	 * 提示窗口
	 * 
	 * @param title 标题
	 * @param message 内容
	 */
	public GuiHandler alert(String title, String message) {
		this.alert(title, message, SnailAlertType.info);
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
		this.event(Type.alert, title, message, type);
		return this;
	}
	
	/**
	 * 提示消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 */
	public GuiHandler notice(String title, String message) {
		this.notice(title, message, SnailNoticeType.info);
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
		this.event(Type.notice, title, message, type);
		return this;
	}
	
	/**
	 * 刷新任务列表
	 */
	public GuiHandler refreshTaskList() {
		event(Type.refreshTaskList);
		return this;
	}
	
	/**
	 * 刷新任务状态
	 */
	public GuiHandler refreshTaskStatus() {
		event(Type.refreshTaskStatus);
		return this;
	}

	/**
	 * 创建窗口
	 */
	public GuiHandler build() {
		event(Type.build);
		return this;
	}
	
	/**
	 * 种子文件选择
	 */
	public GuiHandler torrent(TaskSession taskSession) {
		event(Type.torrent, taskSession);
		return this;
	}

	/**
	 * 显示窗口
	 */
	public GuiHandler show() {
		this.event(Type.show);
		return this;
	}
	
	/**
	 * 隐藏窗口
	 */
	public GuiHandler hide() {
		this.event(Type.hide);
		return this;
	}
	
	/**
	 * 退出
	 */
	public GuiHandler exit() {
		this.event(Type.exit);
		return this;
	}
	
	/**
	 * 执行事件
	 * 
	 * @param type 事件类型
	 * @param args 参数
	 */
	public GuiHandler event(GuiEvent.Type type, Object ... args) {
		if(type == null) {
			return this;
		}
		final GuiEvent event = handler(type);
		if(event == null) {
			LOGGER.warn("未知事件：{}", type);
			return this;
		}
		event.execute(this.gui, args);
		return this;
	}
	
	/**
	 * 注册外部GUI消息代理
	 */
	public boolean messageHandler(IMessageHandler messageHandler) {
		if(this.gui) {
			LOGGER.debug("已经启用本地GUI，忽略注册外部GUI消息代理。");
			return false;
		} else {
			LOGGER.debug("注册外部GUI消息代理");
			this.messageHandler = messageHandler;
			return true;
		}
	}
	
	/**
	 * 发送外部GUI消息
	 */
	public void sendGuiMessage(ApplicationMessage message) {
		if(this.messageHandler != null && message != null) {
			try {
				this.messageHandler.send(message.toString());
			} catch (NetException e) {
				LOGGER.error("发送外部GUI消息异常", e);
			}
		}
	}
	
	/**
	 * 阻塞锁
	 */
	public void lock() {
		synchronized (this.lock) {
			ThreadUtils.wait(this.lock, Duration.ofDays(LOCK_DAYS));
		}
	}
	
	/**
	 * 唤醒阻塞锁：退出程序
	 */
	public void unlock() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}
	
}
