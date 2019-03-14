package com.acgist.snail.gui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * 抽象菜单
 */
public abstract class AbstractMenu extends ContextMenu {

	/**
	 * 初始化基本样式：<br>
	 * 	边框：1
	 * 	透明：0.98
	 */
	protected void init() {
		this.setOpacity(0.94);
		this.setStyle("-fx-padding:1;");
	}
	
	/**
	 * 添加按钮
	 */
	protected void addMenu(MenuItem menuItem) {
		this.getItems().add(menuItem);
	}
	
	/**
	 * 添加分割线
	 */
	protected void addSeparator() {
		this.addMenu(new SeparatorMenuItem());
	}
	
	protected abstract void buildMenu();
	
}
