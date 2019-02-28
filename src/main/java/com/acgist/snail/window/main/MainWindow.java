package com.acgist.snail.window.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.SystemConfig;
import com.acgist.snail.window.AbstractWindow;
import com.acgist.snail.window.about.AboutWindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 主界面
 * TODO：主页隐藏时不刷新任务列表
 */
public class MainWindow extends AbstractWindow<MainController> {

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
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/MainPane.fxml"));
		this.controller = loader.getController();
		BorderPane root = loader.load();
		Scene scene = new Scene(root, 1000, 600);
		stage.setScene(scene);
		stage.setTitle(SystemConfig.getName());
		commonWindow();
	}
	
	@Override
	public void show() {
		stage.setIconified(false); // 防止缩小到任务栏导致不能显示到显示器
		super.show();
	}

}
