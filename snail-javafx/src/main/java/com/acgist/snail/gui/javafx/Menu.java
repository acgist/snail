package com.acgist.snail.gui.javafx;

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
	 * <p>添加菜单</p>
	 * 
	 * @param menuItem 菜单
	 */
	protected void addMenu(MenuItem menuItem) {
		this.getItems().add(menuItem);
	}
	
	/**
	 * <p>添加菜单分隔</p>
	 */
	protected void addSeparator() {
		this.addMenu(new SeparatorMenuItem());
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
		if(icon == null) {
			return new MenuItem(value);
		} else {
			return new MenuItem(value, icon.iconLabel());
		}
	}
	
	/**
	 * <p>初始化所有菜单</p>
	 */
	protected abstract void initMenu();
	
}
