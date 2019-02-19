package com.acgist.killer.window.main;

import com.acgist.killer.window.AbstractWindow;
import com.acgist.killer.window.menu.TrayMenu;

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
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = FXMLLoader.load(this.getClass().getResource("/fxml/MainPane.fxml"));
		Scene scene = new Scene(root, 1000, 600);
		primaryStage.setScene(scene);
		primaryStage.setTitle("ACGIST-KILLER");
		commonStage(primaryStage);
		enableTray(primaryStage);
		primaryStage.show();
	}
	
	/**
	 * 系统图标
	 */
	public void enableTray(Stage stage) {
		TrayMenu.getInstance(stage);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
