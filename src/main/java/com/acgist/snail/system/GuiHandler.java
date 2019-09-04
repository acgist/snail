package com.acgist.snail.system;

/**
 * GUI接口
 * 
 * @author acgist
 * @since 1.1.0
 */
public class GuiHandler {

	/**
	 * 使用本地GUI：JavaFX
	 */
	private final boolean gui;
	
	private GuiHandler(boolean gui) {
		this.gui = gui;
	}
	
	public static final GuiHandler newInstance(boolean gui) {
		return new GuiHandler(gui);
	}
	
	/**
	 * 提示框
	 * 
	 * @param title 标题
	 * @param content 内容
	 */
	public void alert(String title, String content) {
	}
	
	/**
	 * 提示消息
	 * 
	 * @param title 标题
	 * @param content 内容
	 */
	public void notice(String title, String content) {
	}
	
	/**
	 * 刷新（任务）
	 */
	public void refresh() {
	}

	/**
	 * 创建GUI
	 */
	public void build() {
	}
	
}
