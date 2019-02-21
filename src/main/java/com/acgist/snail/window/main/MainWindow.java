package com.acgist.snail.window.main;

import com.acgist.snail.module.config.SystemConfig;
import com.acgist.snail.window.AbstractWindow;
import com.acgist.snail.window.menu.TrayMenu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 主界面
 */
public class MainWindow extends Application implements AbstractWindow {

//	private static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);
	
	private static Stage stage;
	
	@Override
	public void start(Stage stage) throws Exception {
		BorderPane root = FXMLLoader.load(this.getClass().getResource("/fxml/MainPane.fxml"));
		Scene scene = new Scene(root, 1000, 600);
		stage.setScene(scene);
		stage.setTitle(SystemConfig.getName());
		commonWindow(stage);
		enableTray(stage);
		stage.show();
		MainWindow.stage = stage;
	}
	
	/**
	 * 系统图标
	 */
	private void enableTray(Stage stage) {
		TrayMenu.getInstance(stage);
	}
	
	/**
	 * 显示窗口
	 */
	public static final void show() {
		MainWindow.stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
