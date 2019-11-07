package com.acgist.snail.gui.build;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.Alerts;
import com.acgist.snail.gui.Choosers;
import com.acgist.snail.gui.Controller;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;

/**
 * 新建窗口控制器
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class BuildController extends Controller implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildController.class);
	
	@FXML
	private FlowPane root;
	@FXML
	private TextField urlValue;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 文件拖拽
		this.root.setOnDragOver(this.dragOverAction);
		this.root.setOnDragDropped(this.dragDroppedAction);
	}
	
	/**
	 * 选择种子按钮
	 */
	@FXML
	public void handleTorrentAction(ActionEvent event) {
		final File file = Choosers.chooseFile(BuildWindow.getInstance().stage(), "选择种子", "种子文件", "*.torrent");
		if (file != null) {
			setUrl(file.getPath());
		}
	}

	/**
	 * 确认下载按钮
	 */
	@FXML
	public void handleBuildAction(ActionEvent event) {
		final String url = this.urlValue.getText();
		if(StringUtils.isEmpty(url)) {
			return;
		}
		boolean ok = true;
		try {
			// TODO：优化卡死现象
			DownloaderManager.getInstance().newTask(url);
		} catch (DownloadException e) {
			LOGGER.error("新建下载任务异常：{}", url, e);
			ok = false;
			Alerts.warn("下载失败", e.getMessage());
		}
		if(ok) { // 新建下载成功
			setUrl("");
			BuildWindow.getInstance().hide();
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
		if(url != null) {
			this.urlValue.setText(url.trim());
		}
	}
	
	/**
	 * 拖入文件事件（显示）
	 */
	private EventHandler<DragEvent> dragOverAction = (event) -> {
		if (event.getGestureSource() != this.root) {
			final String url = dragboard(event);
			if(ProtocolManager.getInstance().support(url)) {
				event.acceptTransferModes(TransferMode.COPY);
			} else {
				event.acceptTransferModes(TransferMode.NONE);
			}
		}
		event.consume();
	};
	
	/**
	 * 拖入文件事件（加载）
	 */
	private EventHandler<DragEvent> dragDroppedAction = (event) -> {
		final String url = dragboard(event);
		if(StringUtils.isNotEmpty(url)) {
			setUrl(url);
		}
		event.setDropCompleted(true);
		event.consume();
	};
	
}
