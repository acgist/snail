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
		
		show, // 显示窗口
		hide, // 隐藏窗口
		exit, // 退出窗口
		build, // 创建窗口
		alert, // 窗口信息
		notice, // 提示信息
		torrent, // 种子文件选择
		refreshTaskList, // 刷新任务列表：添加、删除
		refreshTaskStatus; // 刷新任务状态：开始、暂停
		
	}

	protected final Type type;
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
	 */
	protected abstract void executeNative(Object ... args);
	
	/**
	 * 扩展GUI
	 */
	protected abstract void executeExtend(Object ... args);
	
	public Type type() {
		return this.type;
	}
	
	public String name() {
		return this.name;
	}
	
}
