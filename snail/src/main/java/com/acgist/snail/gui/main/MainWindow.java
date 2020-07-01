package com.acgist.snail.gui.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;
import com.acgist.snail.gui.statistics.StatisticsWindow;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.DesktopUtils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>主窗口</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class MainWindow extends Window<MainController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);
	
	private static final MainWindow INSTANCE;
	
	static {
		LOGGER.debug("初始化主窗口");
		INSTANCE = new MainWindow();
	}
	
	private MainWindow() {
	}

	public static final MainWindow getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, SystemConfig.getName(), 1000, 600, "/fxml/main.fxml", Modality.NONE);
		this.icon();
		this.help();
		this.statistics();
	}
	
	@Override
	public void show() {
		super.maximize();
		super.show();
	}

	/**
	 * <p>F1：帮助</p>
	 */
	private void help() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if(event.getCode() == KeyCode.F1) {
				DesktopUtils.browse(SystemConfig.getSupport());
			}
		});
	}
	
	/**
	 * <p>F12：统计</p>
	 */
	private void statistics() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if(event.getCode() == KeyCode.F12) {
				StatisticsWindow.getInstance().statistics();
			}
		});
	}
	
}
