package com.acgist.snail.gui.setting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>设置窗口</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class SettingWindow extends Window<SettingController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettingWindow.class);
	
	private static final SettingWindow INSTANCE;
	
	static {
		LOGGER.debug("初始化设置窗口");
		INSTANCE = new SettingWindow();
	}
	
	private SettingWindow() {
	}

	public static final SettingWindow getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "设置", 600, 400, "/fxml/setting.fxml", Modality.APPLICATION_MODAL);
		this.disableResize();
		this.dialogWindow();
	}
	
}