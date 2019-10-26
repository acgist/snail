package com.acgist.snail.gui.event;

/**
 * GUI事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class GuiEvent {

	/**
	 * 事件类型
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
	 * 事件类型
	 */
	protected final Type type;
	/**
	 * 事件名称
	 */
	protected final String name;
	
	protected GuiEvent(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * 执行事件
	 * 
	 * @param gui 本地事件：true-本地；false-扩展；
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
	 * 本地GUI
	 * 
	 * @param args 参数
	 */
	protected abstract void executeNative(Object ... args);
	
	/**
	 * 扩展GUI
	 * 
	 * @param args 参数
	 */
	protected abstract void executeExtend(Object ... args);
	
	public Type type() {
		return this.type;
	}
	
	public String name() {
		return this.name;
	}
	
}
