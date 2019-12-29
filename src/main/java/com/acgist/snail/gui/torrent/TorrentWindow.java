package com.acgist.snail.gui.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Window;
import com.acgist.snail.pojo.ITaskSession;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * <p>编辑任务窗口</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentWindow extends Window<TorrentController> {

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
		windowHidden();
	}
	
	/**
	 * <p>显示下载任务信息</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void show(ITaskSession taskSession) {
		this.controller.tree(taskSession);
		this.showAndWait();
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