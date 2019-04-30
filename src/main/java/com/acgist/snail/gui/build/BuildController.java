package com.acgist.snail.gui.build;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Alerts;
import com.acgist.snail.gui.Choosers;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.DownloaderManager;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.StringUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;

/**
 * 新建窗口控制器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class BuildController implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildController.class);
	
	@FXML
    private FlowPane root;
	@FXML
	private TextField urlValue;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 文件拖拽
		root.setOnDragOver(dragOverAction);
		root.setOnDragDropped(dragDroppedAction);
	}
	
	/**
	 * 选择种子按钮
	 */
	@FXML
	public void handleTorrentAction(ActionEvent event) {
		final File file = Choosers.chooseFile(BuildWindow.getInstance().stage(), "选择种子文件", "种子文件", "*.torrent");
		if (file != null) {
			setUrl(file.getPath());
		}
	}

	/**
	 * 确认下载按钮
	 */
	@FXML
	public void handleBuildAction(ActionEvent event) {
		String url = urlValue.getText();
		if(StringUtils.isEmpty(url)) {
			return;
		}
		boolean ok = true;
		try {
			DownloaderManager.submit(url);
		} catch (DownloadException e) {
			LOGGER.error("新建下载任务异常：{}", url, e);
			ok = false;
			Alerts.warn("下载失败", e.getMessage());
		}
		if(ok) { // 下载成功
			setUrl("");
			BuildWindow.getInstance().hide();
			TaskDisplay.getInstance().refreshTaskTable();
		}
	}

	/**
	 * 取消下载按钮
	 */
	@FXML
	public void handleCancelAction(ActionEvent event) {
		setUrl("");
		BuildWindow.getInstance().hide();
	}
	
	/**
	 * 设置下载地址
	 */
	public void setUrl(String url) {
		urlValue.setText(url);
	}
	
	private EventHandler<DragEvent> dragOverAction = (event) -> {
		if (event.getGestureSource() != root) {
			Dragboard dragboard = event.getDragboard();
			if(dragboard.hasFiles()) {
				File file = dragboard.getFiles().get(0);
				if(TorrentSessionManager.verify(file.getPath())) {
					event.acceptTransferModes(TransferMode.COPY);
				} else {
					event.acceptTransferModes(TransferMode.NONE);
				}
			} else {
				event.acceptTransferModes(TransferMode.NONE);
			}
		}
		event.consume();
	};
	
	private EventHandler<DragEvent> dragDroppedAction = (event) -> {
		Dragboard dragboard = event.getDragboard();
		if (dragboard.hasFiles()) {
			File file = dragboard.getFiles().get(0);
			setUrl(file.getPath());
		}
		event.setDropCompleted(true);
		event.consume();
	};
	
}
