package com.acgist.snail.gui.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemConsole;
import com.acgist.snail.utils.BrowseUtils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 主窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public class MainWindow extends Window<MainController> {

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
		icon();
		help();
		console();
	}
	
	@Override
	public void show() {
		super.maximize();
		super.show();
	}

	/**
	 * F1帮助
	 */
	private void help() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if(event.getCode() == KeyCode.F1) {
				BrowseUtils.open(SystemConfig.getSupport());
			}
		});
	}
	
	/**
	 * F12控制台
	 */
	private void console() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if(event.getCode() == KeyCode.F12) {
				SystemConsole.getInstance().console();
			}
		});
	}
	
}
