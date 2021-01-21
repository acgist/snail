package com.acgist.snail.gui.javafx.window.torrent;

import com.acgist.snail.gui.javafx.window.AbstractWindow;
import com.acgist.snail.pojo.ITaskSession;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * <p>编辑任务窗口</p>
 * 
 * @author acgist
 */
public final class TorrentWindow extends AbstractWindow<TorrentController> {

	private static final TorrentWindow INSTANCE;
	
	public static final TorrentWindow getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new TorrentWindow();
	}
	
	private TorrentWindow() {
		super("编辑任务", 800, 600, "/fxml/torrent.fxml");
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.buildWindow(stage, Modality.APPLICATION_MODAL);
		this.dialogWindow();
		this.windowHidden();
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
	
	/**
	 * <p>窗口隐藏：释放资源</p>
	 */
	private void windowHidden() {
		this.stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, event -> {
			this.controller.release();
		});
	}
	
}