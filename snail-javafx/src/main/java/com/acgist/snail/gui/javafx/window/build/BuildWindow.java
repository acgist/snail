package com.acgist.snail.gui.javafx.window.build;

import com.acgist.snail.gui.javafx.window.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>新建窗口</p>
 * 
 * @author acgist
 */
public final class BuildWindow extends Window<BuildController> {

	private static final BuildWindow INSTANCE;
	
	public static final BuildWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new BuildWindow();
	}
	
	private BuildWindow() {
		super("新建下载", 600, 300, Modality.APPLICATION_MODAL, "/fxml/build.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
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
	
	@Override
	public void show() {
		super.show();
		this.controller.setFocus();
	}
	
	@Override
	public void hide() {
		super.hide();
		this.controller.cleanUrl();
	}
	
}