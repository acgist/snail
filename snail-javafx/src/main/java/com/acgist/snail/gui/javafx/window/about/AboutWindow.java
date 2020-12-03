package com.acgist.snail.gui.javafx.window.about;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.javafx.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>关于窗口</p>
 * 
 * @author acgist
 */
public final class AboutWindow extends Window<AboutController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AboutWindow.class);
	
	private static final AboutWindow INSTANCE;
	
	public static final AboutWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		LOGGER.debug("初始化关于窗口");
		INSTANCE = new AboutWindow();
	}
	
	private AboutWindow() {
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "关于", 600, 300, "/fxml/about.fxml", Modality.APPLICATION_MODAL);
		this.dialogWindow();
	}
	
}