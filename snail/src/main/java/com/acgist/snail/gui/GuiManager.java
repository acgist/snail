package com.acgist.snail.gui;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.GuiEvent.Type;
import com.acgist.snail.net.IMessageHandler;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>GUI管理器</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class GuiManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GuiManager.class);
	
	private static final GuiManager INSTANCE = new GuiManager();
	
	/**
	 * <p>运行模式</p>
	 */
	public enum Mode {
		
		/**
		 * <p>本地模式：本地GUI</p>
		 * <p>本地GUI：JavaFX</p>
		 */
		NATIVE,
		/**
		 * <p>后台模式：扩展GUI</p>
		 * <p>扩展GUI：自定义实现，通过系统消息和系统通知来完成系统管理和任务管理。</p>
		 */
		EXTEND;
		
	}
	
	/**
	 * <p>消息类型</p>
	 */
	public enum MessageType {
		
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
		
	}
	
	/**
	 * <p>事件列表Map</p>
	 * <p>事件类型=事件</p>
	 */
	private static final Map<GuiEvent.Type, GuiEvent> EVENTS = new EnumMap<>(GuiEvent.Type.class);
	/**
	 * <p>扩展GUI阻塞锁时间（天）：{@value}</p>
	 */
	private static final int LOCK_DAYS = 365;
	/**
	 * <p>启动参数：运行模式</p>
	 */
	private static final String ARGS_MODE = "mode";
	
	/**
	 * <p>运行模式</p>
	 * <p>默认：本地GUI</p>
	 */
	private Mode mode = Mode.NATIVE;
	/**
	 * <p>种子文件选择列表（B编码）</p>
	 */
	private String files;
	/**
	 * <p>扩展GUI阻塞锁</p>
	 * <p>使用扩展GUI时阻止程序关闭</p>
	 */
	private final Object lock = new Object();
	/**
	 * <p>扩展GUI消息代理</p>
	 */
	private IMessageHandler extendGuiMessageHandler;
	
	private GuiManager() {
	}
	
	public static final GuiManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>注册事件</p>
	 * 
	 * @param event 事件
	 */
	public static final void register(GuiEvent event) {
		LOGGER.debug("注册GUI事件：{}-{}", event.type(), event.name());
		EVENTS.put(event.type(), event);
	}

	/**
	 * <p>初始化GUI管理器</p>
	 * 
	 * @param args 参数
	 * 
	 * @return GUI管理器
	 */
	public GuiManager init(String ... args) {
		if(args == null) {
			// 没有参数
		} else {
			LOGGER.info("启动参数：{}", String.join(",", args));
			String value;
			for (String arg : args) {
				// 运行模式
				value = StringUtils.argValue(arg, ARGS_MODE);
				if(Mode.EXTEND.name().equalsIgnoreCase(value)) {
					this.mode = Mode.EXTEND;
				}
				LOGGER.info("运行模式：{}", this.mode);
			}
		}
		return this;
	}
	
	/**
	 * <p>显示窗口</p>
	 * 
	 * @return GUI管理器
	 */
	public GuiManager show() {
		this.event(Type.SHOW);
		return this;
	}
	
	/**
	 * <p>隐藏窗口</p>
	 * 
	 * @return GUI管理器
	 */
	public GuiManager hide() {
		this.event(Type.HIDE);
		return this;
	}
	
	/**
	 * <p>退出窗口</p>
	 * 
	 * @return GUI管理器
	 */
	public GuiManager exit() {
		this.event(Type.EXIT);
		return this;
	}

	/**
	 * <p>创建窗口</p>
	 * 
	 * @return GUI管理器
	 */
	public GuiManager build() {
		this.event(Type.BUILD);
		return this;
	}
	
	/**
	 * <p>提示窗口</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return GUI管理器
	 */
	public GuiManager alert(String title, String message) {
		this.alert(title, message, GuiManager.MessageType.INFO);
		return this;
	}

	/**
	 * <p>提示窗口</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 * 
	 * @return GUI管理器
	 */
	public GuiManager alert(String title, String message, GuiManager.MessageType type) {
		this.event(Type.ALERT, title, message, type);
		return this;
	}
	
	/**
	 * <p>提示消息</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return GUI管理器
	 */
	public GuiManager notice(String title, String message) {
		this.notice(title, message, GuiManager.MessageType.INFO);
		return this;
	}
	
	/**
	 * <p>提示消息</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 * 
	 * @return GUI管理器
	 */
	public GuiManager notice(String title, String message, GuiManager.MessageType type) {
		this.event(Type.NOTICE, title, message, type);
		return this;
	}
	
	/**
	 * <p>种子文件选择</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return GUI管理器
	 */
	public GuiManager torrent(ITaskSession taskSession) {
		this.event(Type.TORRENT, taskSession);
		return this;
	}
	
	/**
	 * <p>响应消息</p>
	 * 
	 * @param message 消息
	 */
	public GuiManager response(String message) {
		this.event(Type.RESPONSE, message);
		return this;
	}
	
	/**
	 * <p>刷新任务列表</p>
	 * 
	 * @return GUI管理器
	 */
	public GuiManager refreshTaskList() {
		this.event(Type.REFRESH_TASK_LIST);
		return this;
	}
	
	/**
	 * <p>刷新任务状态</p>
	 * 
	 * @return GUI管理器
	 */
	public GuiManager refreshTaskStatus() {
		this.event(Type.REFRESH_TASK_STATUS);
		return this;
	}

	/**
	 * <p>执行事件</p>
	 * 
	 * @param type 类型
	 * @param args 参数
	 * 
	 * @return GUI管理器
	 */
	public GuiManager event(GuiEvent.Type type, Object ... args) {
		if(type == null) {
			return this;
		}
		final GuiEvent event = EVENTS.get(type);
		if(event == null) {
			LOGGER.warn("未知GUI事件：{}", type);
			return this;
		}
		// TODO：线程安全
		event.execute(this.mode, args);
		return this;
	}
	
	/**
	 * <p>获取种子文件选择列表</p>
	 * 
	 * @return 种子文件选择列表（B编码）
	 */
	public String files() {
		return this.files;
	}
	
	/**
	 * <p>设置种子文件选择列表</p>
	 * 
	 * @param files 种子文件选择列表（B编码）
	 */
	public void files(String files) {
		this.files = files;
	}
	
	/**
	 * <p>注册扩展GUI消息代理</p>
	 * 
	 * @param extendGuiMessageHandler 扩展GUI消息代理
	 * 
	 * @return 是否注册成功
	 */
	public boolean extendGuiMessageHandler(IMessageHandler extendGuiMessageHandler) {
		if(this.mode == Mode.NATIVE) {
			LOGGER.debug("已经启用本地GUI：忽略注册扩展GUI消息代理");
			return false;
		} else {
			LOGGER.debug("注册扩展GUI消息代理");
			this.extendGuiMessageHandler = extendGuiMessageHandler;
			return true;
		}
	}
	
	/**
	 * <p>发送扩展GUI消息</p>
	 * 
	 * @param message 扩展GUI消息
	 */
	public void sendExtendGuiMessage(ApplicationMessage message) {
		if(this.extendGuiMessageHandler != null && message != null) {
			try {
				this.extendGuiMessageHandler.send(message.toString());
			} catch (NetException e) {
				LOGGER.error("发送扩展GUI消息异常", e);
			}
		} else {
			LOGGER.warn("发送扩展GUI消息失败");
		}
	}
	
	/**
	 * <p>扩展GUI阻塞锁</p>
	 */
	public void lock() {
		synchronized (this.lock) {
			ThreadUtils.wait(this.lock, Duration.ofDays(LOCK_DAYS));
		}
	}
	
	/**
	 * <p>释放扩展GUI阻塞锁</p>
	 */
	public void unlock() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}

}
