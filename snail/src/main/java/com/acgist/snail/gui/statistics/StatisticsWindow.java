package com.acgist.snail.gui.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * <p>统计窗口</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class StatisticsWindow extends Window<StatisticsController> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsWindow.class);
	
	private static final StatisticsWindow INSTANCE;
	
	static {
		LOGGER.debug("初始化统计窗口");
		INSTANCE = new StatisticsWindow();
	}
	
	private StatisticsWindow() {
	}

	public static final StatisticsWindow getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "统计", 840, 640, "/fxml/statistics.fxml", Modality.APPLICATION_MODAL);
		this.disableResize();
		this.dialogWindow();
		this.windowHidden();
	}

	/**
	 * <p>统计信息</p>
	 */
	public void statistics() {
		this.controller.statistics();
		this.show();
	}
	
	/**
	 * <p>窗口隐藏：释放资源</p>
	 */
	private void windowHidden() {
		this.stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, event -> {
			this.controller.release();
		});
	}
	
}
