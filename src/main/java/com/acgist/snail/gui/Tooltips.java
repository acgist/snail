package com.acgist.snail.gui;

import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * <p>提示框工具</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class Tooltips {

	/**
	 * <p>不允许实例化</p>
	 */
	private Tooltips() {
	}
	
	/**
	 * <p>创建提示框</p>
	 * <p>默认显示时间（毫秒）：200</p>
	 * 
	 * @param value 提示内容
	 * 
	 * @return 提示框
	 */
	public static final Tooltip newTooltip(String value) {
		return newTooltip(value, 200);
	}
	
	/**
	 * <p>创建提示框</p>
	 * 
	 * @param value 提示内容
	 * @param millis 显示时间：毫秒
	 * 
	 * @return 提示框
	 */
	public static final Tooltip newTooltip(String value, int millis) {
		final Tooltip tooltip = new Tooltip(value);
		tooltip.setShowDelay(Duration.millis(millis));
		final Scene scene = tooltip.getScene();
		scene.getRoot().setStyle(Themes.getThemeStyle()); // 设置主题样式
		scene.getStylesheets().add(Controller.FXML_STYLE); // 导入样式文件
		return tooltip;
	}
	
}
