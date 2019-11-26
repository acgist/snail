package com.acgist.snail.gui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;

/**
 * 菜单
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Menu extends ContextMenu {

	/**
	 * <p>初始化</p>
	 * <dl>
	 * 	<dt>基本样式</dt>
	 * 	<dd>透明：0.94</dd>
	 * 	<dd>边框：1</dd>
	 * </dl>
	 */
	protected void init() {
		this.setOpacity(0.94D);
		this.setStyle("-fx-padding:1;");
	}
	
	/**
	 * 添加菜单
	 */
	protected void addMenu(MenuItem menuItem) {
		this.getItems().add(menuItem);
	}
	
	/**
	 * 添加菜单分隔
	 */
	protected void addSeparator() {
		this.addMenu(new SeparatorMenuItem());
	}
	
	/**
	 * 创建菜单
	 * 
	 * @param value 名称
	 * 
	 * @return 菜单
	 */
	protected MenuItem buildMenuItem(String value) {
		return buildMenuItem(value, null);
	}
	
	/**
	 * 创建菜单
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
	 * 初始化所有菜单
	 */
	protected abstract void initMenu();
	
}
