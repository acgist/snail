package com.acgist.snail.gui.javafx.window.build;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.ProtocolContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Choosers;
import com.acgist.snail.gui.javafx.window.AbstractController;
import com.acgist.snail.utils.StringUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;

/**
 * <p>新建窗口控制器</p>
 * 
 * @author acgist
 */
public final class BuildController extends AbstractController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildController.class);
	
	@FXML
	private FlowPane root;
	
	@FXML
	private TextField urlValue;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.root.setOnDragOver(this.dragOverAction);
		this.root.setOnDragDropped(this.dragDroppedAction);
	}
	
	/**
	 * <p>选择种子按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleTorrentAction(ActionEvent event) {
		final File file = Choosers.chooseFile(BuildWindow.getInstance().stage(), "选择种子", "种子文件", "*.torrent");
		if (file != null) {
			this.setUrl(file.getAbsolutePath());
		}
	}

	/**
	 * <p>确认下载按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleBuildAction(ActionEvent event) {
		final String url = this.urlValue.getText();
		if(StringUtils.isEmpty(url)) {
			return;
		}
		boolean success = true;
		try {
			// TODO：优化卡死现象
			TaskContext.getInstance().download(url);
		} catch (Exception e) {
			LOGGER.error("新建下载任务异常：{}", url, e);
			success = false;
			Alerts.warn("下载失败", e.getMessage());
		}
		if(success) {
			this.cleanUrl();
			BuildWindow.getInstance().hide();
		}
	}

	/**
	 * <p>取消下载按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleCancelAction(ActionEvent event) {
		this.cleanUrl();
		BuildWindow.getInstance().hide();
	}
	
	/**
	 * <p>清空下载链接</p>
	 */
	public void cleanUrl() {
		this.setUrl("");
	}
	
	/**
	 * <p>设置下载链接</p>
	 * 
	 * @param url 下载链接
	 */
	public void setUrl(String url) {
		if(url == null) {
			url = "";
		}
		this.urlValue.setText(url.trim());
	}
	
	/**
	 * <p>拖入文件事件（显示）</p>
	 */
	private EventHandler<DragEvent> dragOverAction = event -> {
		if (event.getGestureSource() != this.root) {
			final String url = this.dragboard(event);
			if(ProtocolContext.getInstance().support(url)) {
				event.acceptTransferModes(TransferMode.COPY);
			} else {
				event.acceptTransferModes(TransferMode.NONE);
			}
		}
		event.consume();
	};
	
	/**
	 * <p>拖入文件事件（加载）</p>
	 */
	private EventHandler<DragEvent> dragDroppedAction = event -> {
		final String url = this.dragboard(event);
		if(StringUtils.isNotEmpty(url)) {
			setUrl(url);
		}
		event.setDropCompleted(true);
		event.consume();
	};
	
}
