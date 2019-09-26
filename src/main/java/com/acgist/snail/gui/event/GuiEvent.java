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
		show,
		/** 隐藏窗口 */
		hide,
		/** 退出窗口 */
		exit,
		/** 创建窗口 */
		build,
		/** 窗口信息 */
		alert,
		/** 提示信息 */
		notice,
		/** 种子文件选择 */
		torrent,
		/** 刷新任务列表：添加、删除 */
		refreshTaskList,
		/** 刷新任务状态：开始、暂停 */
		refreshTaskStatus;
		
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
	 * @param gui 本地事件：true-本地；false-外部；
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
	 * @param args 参数数组
	 */
	protected abstract void executeNative(Object ... args);
	
	/**
	 * 扩展GUI
	 * 
	 * @param args 参数数组
	 */
	protected abstract void executeExtend(Object ... args);
	
	public Type type() {
		return this.type;
	}
	
	public String name() {
		return this.name;
	}
	
}
