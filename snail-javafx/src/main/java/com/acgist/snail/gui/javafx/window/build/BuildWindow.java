package com.acgist.snail.gui.javafx.window.build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.javafx.window.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>新建窗口</p>
 * 
 * @author acgist
 */
public final class BuildWindow extends Window<BuildController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BuildWindow.class);
	
	private static final BuildWindow INSTANCE;
	
	public static final BuildWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		LOGGER.debug("初始化新建窗口");
		INSTANCE = new BuildWindow();
	}
	
	private BuildWindow() {
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "新建下载", 600, 300, "/fxml/build.fxml", Modality.APPLICATION_MODAL);
		this.dialogWindow();
	}
	
	/**
	 * <p>显示新建窗口并设置下载链接</p>
	 * 
	 * @param url 下载链接
	 */
	public void show(String url) {
		this.controller.setUrl(url);
		super.show();
	}
	
}