package com.acgist.snail.window;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * 抽象窗口
 */
public abstract class AbstractWindow extends Application {

	protected Stage stage;
	
	public AbstractWindow() {
		stage = new Stage();
	}
	
	/**
	 * 设置ICON
	 */
	protected void icon(Stage stage) {
		stage.getIcons().add(new Image("/image/logo.png"));	
	}
	
	/**
	 * ESC隐藏窗口
	 */
	protected void esc(Stage stage) {
		stage.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if(event.getCode() == KeyCode.ESCAPE) {
				stage.hide();
			}
		});
	}
	
	/**
	 * 设置通用信息
	 */
	protected void commonWindow(Stage stage) {
		icon(stage);
		esc(stage);
	}
	
	/**
	 * 显示窗口
	 */
	public void show() {
		stage.show();
	}
	
	/**
	 * 隐藏窗口
	 */
	public void hide() {
		stage.hide();
	}
	
	/**
	 * 窗口是否显示
	 */
	public boolean isShowing() {
		return stage.isShowing();
	}
	
}
