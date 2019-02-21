package com.acgist.snail.window;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * 抽象窗口
 */
public interface AbstractWindow {

	/**
	 * 设置ICON
	 */
	default void icon(Stage stage) {
		stage.getIcons().add(new Image("/image/logo.png"));	
	}
	
	/**
	 * ESC隐藏窗口
	 */
	default void esc(Stage stage) {
		stage.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if(event.getCode() == KeyCode.ESCAPE) {
				stage.hide();
			}
		});
	}
	
	/**
	 * 设置通用信息
	 */
	default void commonWindow(Stage stage) {
		icon(stage);
		esc(stage);
	}
	
}
