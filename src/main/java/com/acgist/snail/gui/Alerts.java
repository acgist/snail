package com.acgist.snail.gui;

import java.util.Optional;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * <p>提示窗口工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class Alerts {
	
	/**
	 * <p>不允许实例化</p>
	 */
	private Alerts() {
	}
	
	/**
	 * <p>提示窗口</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return 点击按钮类型
	 */
	public static final Optional<ButtonType> info(String title, String message) {
		return build(title, message, AlertType.INFORMATION);
	}
	
	/**
	 * <p>警告窗口</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * 
	 * @return 点击按钮类型
	 */
	public static final Optional<ButtonType> warn(String title, String message) {
		return build(title, message, AlertType.WARNING);
	}
	
	/**
	 * <p>提示窗口</p>
	 * 
	 * @param title 标题
	 * @param message 内容
	 * @param type 类型
	 * 
	 * @return 点击按钮类型
	 */
	public static final Optional<ButtonType> build(String title, String message, AlertType type) {
		final Alert alert = new Alert(type);
		final Scene scene = alert.getDialogPane().getScene();
		// 导入样式文件
		scene.getStylesheets().add(Controller.FXML_STYLE);
		final Stage stage = (Stage) scene.getWindow();
		// 添加图标
		stage.getIcons().add(new Image(Controller.LOGO_ICON));
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		return alert.showAndWait();
	}
	
}
