package com.acgist.snail.gui.javafx.window.quick;

import com.acgist.snail.gui.javafx.window.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>快传窗口</p>
 * 
 * @author acgist
 */
public final class QuickWindow extends Window<QuickController> {

	private static final QuickWindow INSTANCE;
	
	public static final QuickWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new QuickWindow();
	}
	
	private QuickWindow() {
		super("快传", 600, 500, Modality.APPLICATION_MODAL, "/fxml/quick.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.dialogWindow();
	}
	
}