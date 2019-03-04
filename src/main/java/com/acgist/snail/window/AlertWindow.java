package com.acgist.snail.window;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * 提示
 */
public class AlertWindow {

	public static final Optional<ButtonType> info(String title, String content) {
		return build(AlertType.INFORMATION, title, content);
	}
	
	public static final Optional<ButtonType> warn(String title, String content) {
		return build(AlertType.WARNING, title, content);
	}
	
	public static final Optional<ButtonType> build(AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		return alert.showAndWait();
	}

}
