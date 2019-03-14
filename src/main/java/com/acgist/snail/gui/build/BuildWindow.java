package com.acgist.snail.gui.build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.AbstractWindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 新建
 */
public class BuildWindow extends AbstractWindow<BuildController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BuildWindow.class);
	
	private static BuildWindow INSTANCE;
	
	private BuildWindow() {
	}

	public static final BuildWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		synchronized (BuildWindow.class) {
			if(INSTANCE == null) {
				LOGGER.info("初始化新建窗口");
				INSTANCE = new BuildWindow();
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
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/build.fxml"));
		FlowPane root = loader.load();
		this.controller = loader.getController();
		Scene scene = new Scene(root, 600, 300);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("新建下载");
		disableResize();
		dialogWindow();
	}
	
	public void show(String url) {
		controller.setUrl(url);
		this.show();
	}
	
}