package com.acgist.snail.gui.javafx.window.main;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.window.Window;
import com.acgist.snail.gui.javafx.window.statistics.StatisticsWindow;

import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>主窗口</p>
 * 
 * @author acgist
 */
public final class MainWindow extends Window<MainController> {

	private static final MainWindow INSTANCE;
	
	public static final MainWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new MainWindow();
	}
	
	private MainWindow() {
		super(SystemConfig.getName(), 1000, 600, Modality.NONE, "/fxml/main.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.icon();
		// F1：帮助
		this.keyReleased(KeyCode.F1, () -> Desktops.browse(SystemConfig.getSupport()));
		// F12：统计
		this.keyReleased(KeyCode.F12, () -> StatisticsWindow.getInstance().show());
	}
	
	@Override
	public void show() {
		super.top();
		super.show();
	}

}
