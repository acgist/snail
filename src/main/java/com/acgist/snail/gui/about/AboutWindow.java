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
			}
		}
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "关于", 600, 300, "/fxml/about.fxml", Modality.APPLICATION_MODAL);
		disableResize();
		dialogWindow();
	}
	
}