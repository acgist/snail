package com.acgist.snail.gui.about;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 关于窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class AboutWindow extends Window<AboutController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AboutWindow.class);
	
	private static final AboutWindow INSTANCE;
	
	static {
		LOGGER.debug("初始化关于窗口");
		INSTANCE = new AboutWindow();
	}
	
	private AboutWindow() {
	}

	public static final AboutWindow getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "关于", 600, 300, "/fxml/about.fxml", Modality.APPLICATION_MODAL);
		disableResize();
		dialogWindow();
	}
	
}