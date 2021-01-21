package com.acgist.snail.gui.javafx.window.statistics;

import com.acgist.snail.gui.javafx.window.AbstractWindow;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>统计窗口</p>
 * 
 * @author acgist
 */
public final class StatisticsWindow extends AbstractWindow<StatisticsController> {
	
	private static final StatisticsWindow INSTANCE;
	
	public static final StatisticsWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new StatisticsWindow();
	}
	
	private StatisticsWindow() {
		super("统计", 800, 640, "/fxml/statistics.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, Modality.APPLICATION_MODAL);
		this.dialogWindow();
		this.hiddenRelease();
	}
	
	@Override
	public void show() {
		this.controller.statistics();
		super.show();
	}

	/**
	 * @param piecePos 指定下载Piece索引
	 * 
	 * @see StatisticsController#piecePos(int)
	 */
	public void piecePos(int index) {
		this.controller.piecePos(index);
	}
	
}
