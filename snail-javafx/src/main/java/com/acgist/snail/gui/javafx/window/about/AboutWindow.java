package com.acgist.snail.gui.javafx.window.about;

import com.acgist.snail.gui.javafx.window.AbstractWindow;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>关于窗口</p>
 * 
 * @author acgist
 */
public final class AboutWindow extends AbstractWindow<AboutController> {

	private static final AboutWindow INSTANCE;
	
	public static final AboutWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new AboutWindow();
	}
	
	private AboutWindow() {
		super("关于", 600, 500, "/fxml/about.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, Modality.APPLICATION_MODAL);
		this.dialogWindow();
	}
	
}