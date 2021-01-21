package com.acgist.snail.gui.javafx.window.setting;

import com.acgist.snail.gui.javafx.window.AbstractWindow;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>设置窗口</p>
 * 
 * @author acgist
 */
public final class SettingWindow extends AbstractWindow<SettingController> {

	private static final SettingWindow INSTANCE;
	
	public static final SettingWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new SettingWindow();
	}
	
	private SettingWindow() {
		super("设置", 600, 400, "/fxml/setting.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, Modality.APPLICATION_MODAL);
		this.dialogWindow();
	}
	
}