package com.acgist.snail.gui.javafx.window.quick;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Choosers;
import com.acgist.snail.gui.javafx.Clipboards;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.window.Controller;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.quick.QuickClient;
import com.acgist.snail.utils.StringUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 * <p>快传窗口控制器</p>
 * 
 * @author acgist
 */
public final class QuickController extends Controller {
	
	@FXML
	private BorderPane root;
	@FXML
	private TextArea stun;
	@FXML
	private Text file;
	@FXML
	private ProgressBar progress;
	
	/**
	 * 快传客户端
	 */
	private QuickClient quickClient;
	/**
	 * 快传线程池
	 */
	private ExecutorService executor;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.quickClient = new QuickClient();
	}
	
	/**
	 * <p>作者按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleAuthorAction(ActionEvent event) {
		Desktops.browse(SystemConfig.getAuthor());
	}
	
	/**
	 * <p>检测更新按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleUpdateAction(ActionEvent event) {
		if(SystemContext.latestRelease()) {
			Alerts.info("检测更新", "当前已是最新版本");
		} else {
			final Optional<ButtonType> optional = Alerts.build("检测更新", "是否下载最新版本？", GuiContext.MessageType.CONFIRM);
			if(Alerts.ok(optional)) {
				Desktops.browse(SystemConfig.getSource());
			}
		}
	}
	
	@FXML
	public void handleCopyAction(ActionEvent event) {
		Clipboards.copy(this.stun.getText());
	}
	
	@FXML
	public void handlePasteAction(ActionEvent event) {
		this.stun.setText(Clipboards.get());
	}
	
	@FXML
	public void handleConnectAction(ActionEvent event) {
		final String text = this.stun.getText();
		if(StringUtils.isEmpty(text)) {
			Alerts.info("提示", "暂无STUN信息");
			return;
		}
		if(this.executor == null) {
			this.executor = SystemThreadContext.newExecutor(1, 1, 1, 60, "QUICK-");
		} else {
			Alerts.info("提示", "连接中...");
			return;
		}
		this.executor.submit(() -> {
			try {
				this.quickClient.connect(text, this::connect);
			} catch (NetException e) {
				Alerts.warn("连接失败", e.getMessage());
			}
		});
	}
	
	@FXML
	public void handleChooseAction(ActionEvent event) {
		if(!this.quickClient.connect()) {
			Alerts.info("提示", "暂无连接");
			return;
		}
		final File file = Choosers.chooseFile(QuickWindow.getInstance().stage(), "选择文件", "请选择需要传输的文件");
		if(file != null) {
			this.file.setText(file.getAbsolutePath());
		}
	}
	
	@FXML
	public void handleTransportAction(ActionEvent event) {
		if(!this.quickClient.connect()) {
			Alerts.info("提示", "暂无连接");
			return;
		}
		final String path = this.file.getText();
		if(StringUtils.isEmpty(path)) {
			Alerts.info("提示", "选择文件");
			return;
		}
		final File file = new File(path);
		this.executor.submit(() -> {
			try {
				this.quickClient.quick(file, this::progress);
			} catch (NetException e) {
				Alerts.warn("传输失败", e.getMessage());
			}
		});
	}
	
	@FXML
	public void handleCancelAction(ActionEvent event) {
		this.quickClient.close();
		SystemThreadContext.shutdown(this.executor);
		this.executor = null;
	}

	/**
	 * @param connect 连接结果
	 */
	public void connect(Boolean connect) {
	}
	
	/**
	 * @param progress 进度
	 */
	public void progress(Double progress) {
		this.progress.setProgress(progress);
	}
	
}
