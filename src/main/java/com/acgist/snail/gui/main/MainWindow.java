package com.acgist.snail.gui.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;
import com.acgist.snail.gui.about.AboutWindow;
import com.acgist.snail.system.config.SystemConfig;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 主界面
 * TODO：主页隐藏时不刷新任务列表
 * 任务栏按钮
 * 托盘气泡通知
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
				LOGGER.info("初始化主窗口");
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
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/main.fxml"));
		BorderPane root = loader.load();
		this.controller = loader.getController();
		Scene scene = new Scene(root, 1000, 600);
		stage.setScene(scene);
		stage.setTitle(SystemConfig.getName());
		icon();
	}
	
	@Override
	public void show() {
		stage.setIconified(false); // 设置最大化：防止最小化后从托盘显示出来不能正常显示
		super.show();
	}

}
