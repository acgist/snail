package com.acgist.snail.gui.torrent;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.alert.AlertWindow;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.torrent.TorrentCoder;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.JsonUtils;

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
 * TODO：根据文件大小自动选择文件
 * 大于文件平均值
 */
public class TorrentController implements Initializable {
	
	private static final String HIDE_FILE_PREFIX = "_____padding_file"; // 不需要下载的文件前缀
	
	@FXML
    private FlowPane root;
	@FXML
	private Button download;
	@FXML
	private VBox treeBox;
	@FXML
	private VBox downloadBox;
	
	private TaskSession session;
	private FileSelectManager manager;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 设置属性
		downloadBox.prefWidthProperty().bind(root.widthProperty());
		treeBox.prefWidthProperty().bind(root.widthProperty());
		downloadBox.prefHeightProperty().setValue(40D);;
		treeBox.prefHeightProperty().bind(root.heightProperty().subtract(40D));
		// 绑定事件
		download.setOnAction(downloadEvent);
	}

	/**
	 * 显示信息
	 */
	public void tree(TaskSession session) {
		var entity = session.entity();
		this.session = session;
		TreeView<HBox> tree = buildTree();
		Torrent torrent = null;
		TorrentCoder decoder = null;
		try {
			decoder = TorrentCoder.newInstance(entity.getTorrent());
			torrent = decoder.torrentSession().torrent();
		} catch (DownloadException e) {
			AlertWindow.warn("下载出错", "种子文件解析异常");
			return;
		}
		TorrentInfo torrentInfo = torrent.getInfo();
		manager = new FileSelectManager(torrentInfo.getName(), download, tree);
		torrentInfo.files()
		.stream()
		.filter(file -> !file.path().startsWith(HIDE_FILE_PREFIX))
		.sorted((a, b) -> a.path().compareTo(b.path()))
		.forEach(file -> manager.build(file.path(), file.getLength()));
		manager.select(session.downloadTorrentFiles());
	}
	
	/**
	 * 新建树形菜单
	 */
	private TreeView<HBox> buildTree() {
		TreeView<HBox> tree = new TreeView<>();
		tree.setId("tree");
		tree.getStyleClass().add("tree");
		tree.prefWidthProperty().bind(root.widthProperty());
		tree.prefHeightProperty().bind(treeBox.heightProperty());
		treeBox.getChildren().clear();
		treeBox.getChildren().add(tree);
		return tree;
	}
	
	/**
	 * 下载按钮事件
	 */
	private EventHandler<ActionEvent> downloadEvent = (event) -> {
		TaskEntity entity = session.entity();
		var list = manager.description();
		if(list.isEmpty()) {
			AlertWindow.warn("下载提示", "请选择下载文件");
			return;
		}
		entity.setSize(manager.size());
		entity.setDescription(JsonUtils.toJson(list));
		if(entity.getId() != null) { // 已经添加数据库
			TaskRepository repository = new TaskRepository();
			repository.update(entity);
			DownloaderManager.getInstance().refresh(session);
		}
		TaskDisplay.getInstance().refreshTaskData();
		TorrentWindow.getInstance().hide();
	};
	
}
