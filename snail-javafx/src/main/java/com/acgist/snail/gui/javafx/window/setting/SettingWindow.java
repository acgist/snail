package com.acgist.snail.gui.javafx.window.setting;

import com.acgist.snail.gui.javafx.window.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>设置窗口</p>
 * 
 * @author acgist
 */
public final class SettingWindow extends Window<SettingController> {

	private static final SettingWindow INSTANCE;
	
	public static final SettingWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new SettingWindow();
	}
	
	private SettingWindow() {
		super("设置", 600, 400, Modality.APPLICATION_MODAL, "/fxml/setting.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.dialogWindow();
	}
	
}