package com.acgist.snail.gui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * 抽象菜单
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Menu extends ContextMenu {

	/**
	 * <p>初始化基本样式：</p>
	 * <ul>
	 * 	<li>边框：1</li>
	 * 	<li>透明：0.94</li>
	 * </ul>
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
	
	/**
	 * 创建菜单
	 */
	protected abstract void buildMenu();
	
}
