package com.acgist.snail.gui.torrent;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.Alerts;
import com.acgist.snail.gui.Controller;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TaskSession.Status;
import com.acgist.snail.pojo.wrapper.TorrentSelecterWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.exception.DownloadException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * BT任务窗口控制器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentController extends Controller implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentController.class);
	
	@FXML
	private FlowPane root;
	@FXML
	private Button download;
	@FXML
	private VBox treeBox;
	@FXML
	private VBox downloadBox;
	
	private TaskSession taskSession;
	private SelecterManager selecterManager;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 设置属性
		this.downloadBox.prefWidthProperty().bind(this.root.widthProperty());
		this.treeBox.prefWidthProperty().bind(this.root.widthProperty());
		this.downloadBox.prefHeightProperty().setValue(40D);
		this.treeBox.prefHeightProperty().bind(this.root.heightProperty().subtract(40D));
		// 绑定事件
		this.download.setOnAction(this.downloadEvent);
	}

	/**
	 * 显示信息
	 */
	public void tree(TaskSession taskSession) {
		Torrent torrent = null;
		this.taskSession = taskSession;
		var entity = taskSession.entity();
		final TreeView<HBox> tree = buildTree();
		try {
			torrent = TorrentManager.getInstance().newTorrentSession(entity.getTorrent()).torrent();
		} catch (DownloadException e) {
			Alerts.warn("下载出错", "种子文件解析异常");
			return;
		}
		final TorrentInfo torrentInfo = torrent.getInfo();
		this.selecterManager = SelecterManager.newInstance(torrentInfo.getName(), this.download, tree);
		torrentInfo.files().stream()
			.filter(file -> !file.path().startsWith(TorrentInfo.HIDE_FILE_PREFIX))
			.sorted((a, b) -> a.path().compareTo(b.path()))
			.forEach(file -> this.selecterManager.build(file.path(), file.getLength()));
		this.selecterManager.select(taskSession);
	}
	
	/**
	 * 释放资源：文件选择器
	 */
	public void release() {
		this.selecterManager = null;
	}
	
	/**
	 * 新建树形菜单
	 */
	private TreeView<HBox> buildTree() {
		TreeView<HBox> tree = new TreeView<>();
		tree.setId("tree");
		tree.getStyleClass().add("tree");
		tree.prefWidthProperty().bind(this.root.widthProperty());
		tree.prefHeightProperty().bind(this.treeBox.heightProperty());
		this.treeBox.getChildren().clear();
		this.treeBox.getChildren().add(tree);
		return tree;
	}
	
	/**
	 * 下载按钮事件
	 */
	private EventHandler<ActionEvent> downloadEvent = (event) -> {
		final TaskEntity entity = this.taskSession.entity();
		var list = this.selecterManager.description();
		if(list.isEmpty()) {
			Alerts.warn("下载提示", "请选择下载文件");
			return;
		}
		entity.setSize(this.selecterManager.size());
		final TorrentSelecterWrapper wrapper = TorrentSelecterWrapper.newEncoder(list);
		entity.setDescription(wrapper.description());
		if(entity.getId() != null) { // 已经添加数据库
			boolean restart = false;
			if(entity.getType() == Type.magnet) { // 磁力链接转为种子
				restart = true;
				entity.setType(Type.torrent);
				entity.setStatus(Status.await);
				entity.setEndDate(null);
			}
			final TaskRepository repository = new TaskRepository();
			repository.update(entity);
			if(restart) {
				try {
					DownloaderManager.getInstance().changeDownloaderRestart(this.taskSession);
				} catch (DownloadException e) {
					LOGGER.error("添加下载任务异常", e);
				}
			} else {
				DownloaderManager.getInstance().refresh(this.taskSession);
			}
		}
		TaskDisplay.getInstance().refreshTaskStatus();
		TorrentWindow.getInstance().hide();
	};
	
}
