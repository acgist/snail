package com.acgist.snail.window;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 抽象窗口
 */
public interface AbstractWindow {

	/**
	 * 设置ICON
	 */
	default void icon(Stage primaryStage) {
		primaryStage.getIcons().add(new Image("/image/logo.png"));	
	}
	
	/**
	 * 设置通用信息
	 */
	default void commonStage(Stage primaryStage) {
		icon(primaryStage);
	}
	
}
