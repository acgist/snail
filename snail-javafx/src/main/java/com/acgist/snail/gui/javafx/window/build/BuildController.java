package com.acgist.snail.gui.javafx.window.build;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.ProtocolContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Choosers;
import com.acgist.snail.gui.javafx.window.Controller;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.CollectionUtils;
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
public final class BuildController extends Controller {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildController.class);
	
	@FXML
	private FlowPane root;
	@FXML
	private TextField urlValue;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.root.setOnDragOver(this.dragOverAction);
		this.root.setOnDragDropped(this.dragDroppedAction);
		this.urlValue.setPromptText("输入下载链接、磁力链接或选择种子文件");
	}
	
	/**
	 * <p>选择种子按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleTorrentAction(ActionEvent event) {
		final List<File> files = Choosers.chooseMultipleFile(BuildWindow.getInstance().stage(), "选择种子", "种子文件", "*.torrent");
		if (CollectionUtils.isNotEmpty(files)) {
			this.setUrl(files.stream().map(File::getAbsolutePath).collect(Collectors.joining(SymbolConfig.Symbol.SEMICOLON.toString())));
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
			Alerts.warn("下载失败", "输入下载链接");
			return;
		}
		// 使用分号分隔：如果本地文件目录存在空格不能使用
		final String[] urls = url.split(SymbolConfig.Symbol.SEMICOLON.toString());
		boolean success = false;
		for (String value : urls) {
			if(StringUtils.isEmpty(value)) {
				continue;
			}
			success = this.download(value) || success;
		}
		if(success) {
			BuildWindow.getInstance().hide();
		}
	}
	
	/**
	 * <p>下载链接</p>
	 * 
	 * @param url 链接地址
	 * 
	 * @return 是否成功
	 */
	private boolean download(String url) {
		boolean success = true;
		try {
			TaskContext.getInstance().download(url);
		} catch (Exception e) {
			LOGGER.error("新建下载任务异常：{}", url, e);
			Alerts.warn("下载失败", e.getMessage());
			success = false;
		}
		return success;
	}
	
	/**
	 * <p>取消下载按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleCancelAction(ActionEvent event) {
		BuildWindow.getInstance().hide();
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
	 * <p>清空下载链接</p>
	 */
	public void cleanUrl() {
		this.setUrl(null);
	}
	
	/**
	 * <p>设置焦点</p>
	 */
	public void setFocus() {
		this.urlValue.requestFocus();
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
			this.setUrl(url);
		}
		event.setDropCompleted(true);
		event.consume();
	};
	
}
