package com.acgist.snail.gui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;

/**
 * <p>菜单</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Menu extends ContextMenu {

	/**
	 * <p>添加菜单</p>
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
		return buildMenuItem(value, null);
	}
	
	/**
	 * <p>创建菜单</p>
	 * 
	 * @param value 名称
	 * @param icon 图标
	 * 
	 * @return 菜单
	 */
	protected MenuItem buildMenuItem(String value, String icon) {
		if(icon == null) {
			return new MenuItem(value);
		} else {
			return new MenuItem(value, new ImageView(icon));
		}
	}
	
	/**
	 * <p>初始化所有菜单</p>
	 */
	protected abstract void initMenu();
	
}
