package com.acgist.snail.gui.event;

/**
 * <p>GUI事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class GuiEvent {

	/**
	 * <p>GUI事件类型</p>
	 */
	public enum Type {
		
		/** 显示窗口 */
		SHOW,
		/** 隐藏窗口 */
		HIDE,
		/** 退出窗口 */
		EXIT,
		/** 创建窗口 */
		BUILD,
		/** 窗口信息 */
		ALERT,
		/** 提示信息 */
		NOTICE,
		/** 种子文件选择 */
		TORRENT,
		/** 刷新任务列表：添加、删除 */
		REFRESH_TASK_LIST,
		/** 刷新任务状态：开始、暂停 */
		REFRESH_TASK_STATUS;
		
	}

	/**
	 * <p>事件类型</p>
	 */
	protected final Type type;
	/**
	 * <p>事件名称</p>
	 */
	protected final String name;
	
	protected GuiEvent(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * <p>执行GUI事件</p>
	 * 
	 * @param gui {@code true}-本地GUI；{@code false}-扩展GUI；
	 * @param args 参数
	 */
	public void execute(boolean gui, Object ... args) {
		if(gui) {
			this.executeNative(args);
		} else {
			this.executeExtend(args);
		}
	}
	
	/**
	 * <p>执行本地GUI事件</p>
	 * 
	 * @param args 参数
	 */
	protected abstract void executeNative(Object ... args);
	
	/**
	 * <p>执行扩展GUI事件</p>
	 * 
	 * @param args 参数
	 */
	protected abstract void executeExtend(Object ... args);
	
	/**
	 * <p>获取事件类型</p>
	 * 
	 * @return 事件类型
	 */
	public Type type() {
		return this.type;
	}
	
	/**
	 * <p>获取事件名称</p>
	 * 
	 * @return 事件名称
	 */
	public String name() {
		return this.name;
	}
	
}
