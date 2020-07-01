package com.acgist.snail.gui.javafx;

import java.util.Optional;

import com.acgist.snail.gui.GuiManager.SnailAlertType;

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
		return build(title, message, SnailAlertType.INFO);
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
		return build(title, message, SnailAlertType.WARN);
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
	public static final Optional<ButtonType> build(String title, String message, SnailAlertType type) {
		final Alert alert = new Alert(getAlertType(type));
		final Scene scene = alert.getDialogPane().getScene();
		Window.applyTheme(scene);
		final Stage stage = (Stage) scene.getWindow();
		stage.getIcons().add(new Image(Controller.LOGO_ICON_200)); // 设置图标
		alert.setTitle(title);
//		alert.setGraphic(null); // 去掉图标
		alert.setHeaderText(null); // 去掉头部
		alert.setContentText(message);
		return alert.showAndWait();
	}
	
	/**
	 * <p>获取JavaFX窗口类型</p>
	 * 
	 * @return JavaFX窗口类型
	 */
	private static final AlertType getAlertType(SnailAlertType type) {
		switch (type) {
		case NONE:
			return AlertType.NONE;
		case INFO:
			return AlertType.INFORMATION;
		case WARN:
			return AlertType.WARNING;
		case CONFIRM:
			return AlertType.CONFIRMATION;
		case ERROR:
			return AlertType.ERROR;
		default:
			return AlertType.INFORMATION;
		}
	}
	
}
