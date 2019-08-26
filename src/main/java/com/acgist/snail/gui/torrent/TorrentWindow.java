package com.acgist.snail.gui.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;
import com.acgist.snail.pojo.session.TaskSession;

import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * BT任务窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentWindow extends Window<TorrentController> {

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
				LOGGER.debug("初始化编辑任务窗口");
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
		final FlowPane root = super.loadFxml("/fxml/torrent.fxml");
		final Scene scene = new Scene(root, 800, 600);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("编辑任务");
		disableResize();
		dialogWindow();
	}
	
	/**
	 * 显示下载任务信息
	 */
	public void show(TaskSession taskSession) {
		this.controller.tree(taskSession);
		this.showAndWait();
		this.controller.release();
	}
	
}