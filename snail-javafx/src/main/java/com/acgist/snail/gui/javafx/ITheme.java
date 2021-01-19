package com.acgist.snail.gui.javafx;

import javafx.scene.paint.Color;

/**
 * <p>系统主题</p>
 * 
 * @author acgist
 */
public interface ITheme {
	
	/**
	 * <p>红色：禁用</p>
	 */
	public static final Color COLOR_RED = Color.rgb(0xDD, 0x33, 0x55);
	/**
	 * <p>灰色：禁用</p>
	 */
	public static final Color COLOR_GRAY = Color.rgb(0xCC, 0xCC, 0xCC);
	/**
	 * <p>灰色：可用</p>
	 */
	public static final Color COLOR_BLUD = Color.rgb(0, 153, 204);
	/**
	 * <p>绿色：可用</p>
	 */
	public static final Color COLOR_GREEN = Color.rgb(0x22, 0xAA, 0x22);
	/**
	 * <p>黄色：警告</p>
	 */
	public static final Color COLOR_YELLOW = Color.rgb(0xFF, 0xEE, 0x99);
	
	/**
	 * <p>获取系统主题颜色</p>
	 * 
	 * @return 系统主题颜色
	 */
	Color systemThemeColor();

}
