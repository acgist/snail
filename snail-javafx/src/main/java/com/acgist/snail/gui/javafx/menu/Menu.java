package com.acgist.snail.gui.javafx.menu;

import com.acgist.snail.gui.javafx.Fonts.SnailIcon;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * <p>菜单</p>
 * 
 * @author acgist
 */
public abstract class Menu extends ContextMenu {

	/**
	 * <p>添加分隔</p>
	 */
	protected void buildSeparator() {
		this.getItems().add(new SeparatorMenuItem());
	}
	
	/**
	 * <p>新建菜单</p>
	 * 
	 * @param value 名称
	 * @param icon 图标
	 * @param action 事件
	 * 
	 * @return 菜单
	 */
	protected MenuItem buildMenuItem(String value, SnailIcon icon, EventHandler<ActionEvent> action) {
		final MenuItem menuItem;
		if(icon == null) {
			menuItem = new MenuItem(value);
		} else {
			menuItem = new MenuItem(value, icon.iconLabel());
		}
		this.getItems().add(menuItem);
		menuItem.setOnAction(action);
		return menuItem;
	}
	
	/**
	 * <p>新建所有菜单</p>
	 */
	protected abstract void buildMenus();
	
}
