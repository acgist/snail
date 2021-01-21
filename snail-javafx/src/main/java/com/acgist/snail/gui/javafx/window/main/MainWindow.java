package com.acgist.snail.gui.javafx.window.main;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.window.AbstractWindow;
import com.acgist.snail.gui.javafx.window.statistics.StatisticsWindow;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>主窗口</p>
 * 
 * @author acgist
 */
public final class MainWindow extends AbstractWindow<MainController> {

	private static final MainWindow INSTANCE;
	
	public static final MainWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new MainWindow();
	}
	
	private MainWindow() {
		super(SystemConfig.getName(), 1000, 600, "/fxml/main.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, Modality.NONE);
		this.icon();
		this.help();
		this.statistics();
	}
	
	@Override
	public void show() {
		super.top();
		super.show();
	}

	/**
	 * <p>F1：帮助</p>
	 */
	private void help() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if(event.getCode() == KeyCode.F1) {
				Desktops.browse(SystemConfig.getSupport());
			}
		});
	}
	
	/**
	 * <p>F12：统计</p>
	 */
	private void statistics() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if(event.getCode() == KeyCode.F12) {
				StatisticsWindow.getInstance().show();
			}
		});
	}
	
}
