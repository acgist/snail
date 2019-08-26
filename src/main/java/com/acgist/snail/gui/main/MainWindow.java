package com.acgist.snail.gui.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;
import com.acgist.snail.gui.about.AboutWindow;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemConsole;
import com.acgist.snail.utils.BrowseUtils;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 主窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public class MainWindow extends Window<MainController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);
	
	private static MainWindow INSTANCE;
	
	private MainWindow() {
	}

	public static final MainWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		synchronized (AboutWindow.class) {
			if(INSTANCE == null) {
				LOGGER.debug("初始化主窗口");
				INSTANCE = new MainWindow();
				try {
					INSTANCE.start(INSTANCE.stage);
				} catch (Exception e) {
					LOGGER.error("窗口初始化异常", e);
				}
			}
		}
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		final BorderPane root = super.loadFxml("/fxml/main.fxml");
		final Scene scene = new Scene(root, 1000, 600);
		stage.setScene(scene);
		stage.setTitle(SystemConfig.getName());
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
	 * F12控制台（输出系统状态信息到日志）
	 */
	private void console() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if(event.getCode() == KeyCode.F12) {
				SystemConsole.getInstance().console();
			}
		});
	}
	
}
