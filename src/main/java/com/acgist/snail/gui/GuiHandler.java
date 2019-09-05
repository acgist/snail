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
	public static final String GUI = "gui";
	/**
	 * 后台模式
	 */
	public static final String DAEMO = "daemo";
	
	private final int lockDays = 365;
	private final Object lock = new Object();
	
	/**
	 * alert提示窗口类型
	 */
	public enum SnailAlertType {
		
		none, // 普通
		info, // 提示
		warn, // 警告
		confirm, // 确认
		error; // 错误
		
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
			}
			return AlertType.INFORMATION;
		}
		
	}
	
	/**
	 * notice提示消息类型
	 */
	public enum SnailNoticeType {
		
		none, // 普通
		info, // 提示
		warn, // 警告
		error; // 错误
		
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
			}
			return MessageType.INFO;
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
		register(BuildEvent.newInstance());
		register(ShowEvent.newInstance());
		register(HideEvent.newInstance());
		register(ExitEvent.newInstance());
		register(AlertEvent.newInstance());
		register(NoticeEvent.newInstance());
		register(RefreshTaskListEvent.newInstance());
		register(RefreshTaskStatusEvent.newInstance());
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
	public void init(String ... args) {
		if(args == null || args.length < 1) {
			this.gui = true;
		} else {
			final String arg = args[0];
			this.gui = !DAEMO.equalsIgnoreCase(arg);
		}
		LOGGER.debug("运行模式：{}", this.gui ? "本地GUI" : "后台模式");
	}
	
	/**
	 * 提示窗口
	 * 
	 * @param title 标题
	 * @param message 内容
	 */
	public void alert(String title, String message) {
		this.alert(title, message, SnailAlertType.info);
	}

	/**
	 * 提示窗口
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 */
	public void alert(String title, String message, SnailAlertType type) {
		this.event(Type.alert, title, message, type);
	}
	
	/**
	 * 提示消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 */
	public void notice(String title, String message) {
		this.notice(title, message, SnailNoticeType.info);
	}
	
	/**
	 * 提示消息
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 */
	public void notice(String title, String message, SnailNoticeType type) {
		this.event(Type.notice, title, message, type);
	}
	
	/**
	 * 刷新任务列表
	 */
	public void refreshTaskList() {
		event(Type.refreshTaskList);
	}
	
	/**
	 * 刷新任务状态
	 */
	public void refreshTaskStatus() {
		event(Type.refreshTaskStatus);
	}

	/**
	 * 创建窗口
	 */
	public void build() {
		event(Type.build);
	}

	/**
	 * 显示窗口
	 */
	public void show() {
		this.event(Type.show);
	}
	
	/**
	 * 隐藏窗口
	 */
	public void hide() {
		this.event(Type.hide);
	}
	
	/**
	 * 退出
	 */
	public void exit() {
		this.event(Type.exit);
	}
	
	/**
	 * 执行事件
	 * 
	 * @param type 事件类型
	 * @param args 参数
	 */
	public void event(GuiEvent.Type type, Object ... args) {
		if(type == null) {
			return;
		}
		final GuiEvent event = handler(type);
		if(event == null) {
			LOGGER.warn("未知事件：{}", type);
			return;
		}
		event.execute(this.gui, args);
	}
	
	/**
	 * 锁定：防止程序退出
	 */
	public void lock() {
		synchronized (this.lock) {
			ThreadUtils.wait(this.lock, Duration.ofDays(this.lockDays));
		}
	}
	
	/**
	 * 唤醒锁：退出程序
	 */
	public void unlock() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}
	
}
