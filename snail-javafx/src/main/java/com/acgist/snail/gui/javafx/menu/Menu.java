package com.acgist.snail.gui.javafx.menu;

import com.acgist.snail.gui.javafx.Fonts.SnailIcon;

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
	 * <p>创建菜单</p>
	 * 
	 * @param value 名称
	 * 
	 * @return 菜单
	 */
	protected MenuItem buildMenuItem(String value) {
		return this.buildMenuItem(value, null);
	}
	
	/**
	 * <p>创建菜单</p>
	 * 
	 * @param value 名称
	 * @param icon 图标
	 * 
	 * @return 菜单
	 */
	protected MenuItem buildMenuItem(String value, SnailIcon icon) {
		MenuItem menuItem;
		if(icon == null) {
			menuItem = new MenuItem(value);
		} else {
			menuItem = new MenuItem(value, icon.iconLabel());
		}
		this.getItems().add(menuItem);
		return menuItem;
	}
	
	/**
	 * <p>创建所有菜单</p>
	 */
	protected abstract void buildMenus();
	
}
