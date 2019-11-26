package com.acgist.snail.gui;

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
	 * 不允许实例化
	 */
	private Tooltips() {
	}
	
	/**
	 * <p>创建提示框</p>
	 * <p>默认显示时间：200（毫秒）</p>
	 * 
	 * @param value 提示内容
	 * 
	 * @return 提示框
	 */
	public static final Tooltip newTooltip(String value) {
		return newTooltip(value, 200);
	}
	
	/**
	 * 创建提示框
	 * 
	 * @param value 提示内容
	 * @param millis 显示时间：毫秒
	 * 
	 * @return 提示框
	 */
	public static final Tooltip newTooltip(String value, int millis) {
		final Tooltip tooltip = new Tooltip(value);
		tooltip.setShowDelay(Duration.millis(millis));
		tooltip.setStyle("-fx-text-fill:#000;-fx-background-color:#fff;");
		tooltip.setOpacity(0.94D);
		return tooltip;
	}
	
}
