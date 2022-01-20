package com.acgist.snail.gui.javafx.window.statistics;

import com.acgist.snail.gui.javafx.window.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>统计窗口</p>
 * 
 * @author acgist
 */
public final class StatisticsWindow extends Window<StatisticsController> {
	
	private static final StatisticsWindow INSTANCE;
	
	public static final StatisticsWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new StatisticsWindow();
	}
	
	private StatisticsWindow() {
		super("统计", 800, 640, Modality.APPLICATION_MODAL, "/fxml/statistics.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.dialogWindow();
		this.hiddenRelease();
	}
	
	@Override
	public void show() {
		this.controller.statistics();
		super.show();
	}
	
}
