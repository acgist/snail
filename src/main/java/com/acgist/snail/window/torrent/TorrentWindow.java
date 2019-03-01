package com.acgist.snail.window.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.window.AbstractWindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 编辑任务窗口
 */
public class TorrentWindow extends AbstractWindow<TorrentController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentWindow.class);
	
	private static TorrentWindow INSTANCE;
	
	private TorrentWindow() {
	}

	public static final TorrentWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		synchronized (TorrentWindow.class) {
			if(INSTANCE == null) {
				LOGGER.info("初始化编辑任务窗口");
				INSTANCE = new TorrentWindow();
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
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/torrent.fxml"));
		FlowPane root = loader.load();
		this.controller = loader.getController();
		Scene scene = new Scene(root, 800, 600);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("编辑任务");
		disableResize();
		dialogWindow();
	}
	
	public void show(TaskWrapper wrapper) {
		this.controller.tree(wrapper);
		this.show();
	}
	
}