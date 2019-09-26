package com.acgist.snail.gui;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 提示窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Alerts {
	
	/**
	 * 不允许实例化
	 */
	private Alerts() {
	}

	/**
	 * 提示
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return 点击按钮
	 */
	public static final Optional<ButtonType> info(String title, String message) {
		return build(title, message, AlertType.INFORMATION);
	}
	
	/**
	 * 警告
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return 点击按钮
	 */
	public static final Optional<ButtonType> warn(String title, String message) {
		return build(title, message, AlertType.WARNING);
	}
	
	/**
	 * 创建提示窗口
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 * 
	 * @return 点击按钮
	 */
	public static final Optional<ButtonType> build(String title, String message, AlertType type) {
		final Alert alert = new Alert(type);
		final Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("/image/logo.png"));
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		return alert.showAndWait();
	}

}
