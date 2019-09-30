package com.acgist.snail.gui.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;
import com.acgist.snail.pojo.session.TaskSession;

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
	
	private static final TorrentWindow INSTANCE;
	
	static {
		LOGGER.debug("初始化编辑任务窗口");
		INSTANCE = new TorrentWindow();
	}
	
	private TorrentWindow() {
	}

	public static final TorrentWindow getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, "编辑任务", 800, 600, "/fxml/torrent.fxml", Modality.APPLICATION_MODAL);
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