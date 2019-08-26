package com.acgist.snail.gui.about;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 关于窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public class AboutWindow extends Window<AboutController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AboutWindow.class);
	
	private static AboutWindow INSTANCE;
	
	private AboutWindow() {
	}

	public static final AboutWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		synchronized (AboutWindow.class) {
			if(INSTANCE == null) {
				LOGGER.debug("初始化关于窗口");
				INSTANCE = new AboutWindow();
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
		final GridPane root = super.loadFxml("/fxml/about.fxml");
		final Scene scene = new Scene(root, 600, 300);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("关于");
		disableResize();
		dialogWindow();
	}
	
}