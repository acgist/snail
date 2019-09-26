package com.acgist.snail.gui.setting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 设置窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public class SettingWindow extends Window<SettingController> {

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
				LOGGER.debug("初始化设置窗口");
				INSTANCE = new SettingWindow();
			}
		}
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "设置", 600, 600, "/fxml/setting.fxml", Modality.APPLICATION_MODAL);
		disableResize();
		dialogWindow();
	}
	
}