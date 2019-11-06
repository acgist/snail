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
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.pojo.wrapper.TorrentSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
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
 * 编辑任务窗口控制器
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
	
	/**
	 * 任务信息
	 */
	private ITaskSession taskSession;
	/**
	 * 任务文件选择器
	 */
	private SelectorManager selectorManager;
	
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
	public void tree(ITaskSession taskSession) {
		Torrent torrent = null;
		this.taskSession = taskSession;
		final TreeView<HBox> tree = buildTree();
		try {
			torrent = TorrentManager.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
		} catch (DownloadException e) {
			LOGGER.error("种子文件解析异常", e);
			Alerts.warn("下载失败", "种子文件解析失败：" + e.getMessage());
			return;
		}
		final TorrentInfo torrentInfo = torrent.getInfo();
		this.selectorManager = SelectorManager.newInstance(torrent.name(), this.download, tree);
		torrentInfo.files().stream()
			.filter(file -> !file.path().startsWith(TorrentInfo.PADDING_FILE_PREFIX))
			.sorted((a, b) -> a.path().compareTo(b.path()))
			.forEach(file -> this.selectorManager.build(file.path(), file.getLength()));
		this.selectorManager.select(taskSession);
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		this.selectorManager = null;
		this.treeBox.getChildren().clear();
	}
	
	/**
	 * 创建树形菜单
	 */
	private TreeView<HBox> buildTree() {
		final TreeView<HBox> tree = new TreeView<>();
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
		var list = this.selectorManager.description();
		if(list.isEmpty()) {
			Alerts.warn("下载失败", "请选择下载文件");
			return;
		}
		this.taskSession.setSize(this.selectorManager.size());
		final TorrentSelectorWrapper wrapper = TorrentSelectorWrapper.newEncoder(list);
		this.taskSession.setDescription(wrapper.serialize());
		if(this.taskSession.getId() != null) { // 已经添加数据库
			boolean restart = false;
			if(this.taskSession.getType() == Type.MAGNET) { // 磁力链接转为BT任务
				restart = true;
				this.taskSession.setType(Type.TORRENT);
				this.taskSession.setStatus(Status.AWAIT);
				this.taskSession.setEndDate(null);
			}
			// 更新任务
			this.taskSession.update();
			// 切换下载器并且重新下载
			if(restart) {
				try {
					DownloaderManager.getInstance().changeDownloaderRestart(this.taskSession);
				} catch (DownloadException e) {
					LOGGER.error("切换下载器异常", e);
				}
			} else {
				DownloaderManager.getInstance().refresh(this.taskSession);
			}
		}
		TaskDisplay.getInstance().refreshTaskStatus();
		TorrentWindow.getInstance().hide();
	};
	
}
