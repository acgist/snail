package com.acgist.snail.window.about;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.window.AbstractWindow;

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
	}

	static {
		synchronized (AboutWindow.class) {
			if(INSTANCE == null) {
				INSTANCE = new AboutWindow();
				INSTANCE.stage = new Stage();
				try {
					INSTANCE.start(INSTANCE.stage);
				} catch (Exception e) {
					LOGGER.error("窗口初始化异常", e);
				}
			}
		}
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		GridPane root = FXMLLoader.load(this.getClass().getResource("/fxml/AboutPane.fxml"));
		Scene scene = new Scene(root, 600, 400);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("关于");
		commonWindow(stage);
	}
	
	/**
	 * 显示窗口
	 */
	public static final void show() {
		INSTANCE.stage.show();
	}
	
}