package com.acgist.killer.window.about;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.killer.window.AbstractWindow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 关于
 */
public class AboutWindow extends Application implements AbstractWindow {

	private static final Logger LOGGER = LoggerFactory.getLogger(AboutWindow.class);
	
	private Stage stage;
	
	private static AboutWindow INSTANCE;
	
	private AboutWindow() {
		stage = new Stage();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		GridPane root = FXMLLoader.load(this.getClass().getResource("/fxml/AboutPane.fxml"));
		Scene scene = new Scene(root, 600, 400);
		primaryStage.initModality(Modality.APPLICATION_MODAL);
		primaryStage.setScene(scene);
		primaryStage.setTitle("关于");
		commonStage(primaryStage);
		primaryStage.show();
	}

	/**
	 * 显示窗口
	 */
	public static final void show() {
		synchronized (AboutWindow.class) {
			if(INSTANCE == null) {
				INSTANCE = new AboutWindow();
				try {
					INSTANCE.start(INSTANCE.stage);
				} catch (Exception e) {
					LOGGER.error("关于窗口显示异常", e);
				}
			}
		}
		INSTANCE.stage.show();
	}
	
}