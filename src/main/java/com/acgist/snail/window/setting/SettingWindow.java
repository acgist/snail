package com.acgist.snail.window.setting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.window.AbstractWindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 设置窗口
 */
public class SettingWindow extends AbstractWindow<SettingController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettingWindow.class);
	
	private static SettingWindow INSTANCE;
	
	private SettingWindow() {
	}

	public static final SettingWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		synchronized (SettingWindow.class) {
			if(INSTANCE == null) {
				LOGGER.info("初始化设置窗口");
				INSTANCE = new SettingWindow();
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
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/SettingPane.fxml"));
		this.controller = loader.getController();
		FlowPane root = loader.load();
		Scene scene = new Scene(root, 800, 600);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("设置");
		commonWindow();
	}
	
}