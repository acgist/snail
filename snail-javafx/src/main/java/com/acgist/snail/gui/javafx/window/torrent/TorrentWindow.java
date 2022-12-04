package com.acgist.snail.gui.javafx.window.torrent;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.gui.javafx.window.Window;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>编辑任务窗口</p>
 * 
 * @author acgist
 */
public final class TorrentWindow extends Window<TorrentController> {

	private static final TorrentWindow INSTANCE;
	
	public static final TorrentWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new TorrentWindow();
	}
	
	private TorrentWindow() {
		super("编辑任务", 800, 600, Modality.APPLICATION_MODAL, "/fxml/torrent.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.dialogWindow();
		this.hiddenRelease();
	}
	
	/**
	 * <p>显示下载任务信息</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void show(ITaskSession taskSession) {
		this.controller.buildTree(taskSession);
		super.showAndWait();
	}
	
}