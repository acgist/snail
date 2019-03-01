package com.acgist.snail.window.edit;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.coder.torrent.TorrentFiles;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.utils.JSONUtils;
import com.acgist.snail.window.AlertWindow;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EditController implements Initializable {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(EditController.class);
	
	private static final String HIDE_FILE_PREFIX = "_____padding_file"; // 不需要下载的文件前缀
	
	@FXML
    private FlowPane root;
	@FXML
	private Button download;
	@FXML
	private VBox treeBox;
	@FXML
	private VBox downloadBox;
	
	private TaskWrapper wrapper;
	private FileSelectManager manager;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		downloadBox.prefWidthProperty().bind(root.widthProperty());
		treeBox.prefWidthProperty().bind(root.widthProperty());
		downloadBox.prefHeightProperty().setValue(40D);;
		treeBox.prefHeightProperty().bind(root.heightProperty().subtract(40D));
		download.setOnAction(downloadEvent);
	}

	/**
	 * 显示信息
	 */
	public void tree(TaskWrapper wrapper) {
		TreeView<HBox> tree = new TreeView<>();
		tree.setId("tree");
		tree.getStyleClass().add("tree");
		treeBox.getChildren().clear();
		treeBox.getChildren().add(tree);
		tree.prefWidthProperty().bind(root.widthProperty());
		tree.prefHeightProperty().bind(treeBox.heightProperty());
		this.wrapper = wrapper;
		TorrentDecoder decoder = TorrentDecoder.newInstance(wrapper.getTorrent());
		TorrentFiles files = decoder.torrentInfo().getInfo();
		manager = new FileSelectManager(files.getName(), download, tree);
		files.getFiles()
		.stream()
		.filter(file -> !file.path().startsWith(HIDE_FILE_PREFIX))
		.sorted((a, b) -> {
			return a.path().compareTo(b.path());
		})
		.forEach(file -> {
			manager.build(file.path(), file.getLength());
		});
	}
	
	/**
	 * 下载按钮事件
	 */
	private EventHandler<ActionEvent> downloadEvent = (event) -> {
		TaskEntity entity = wrapper.getEntity();
		var list = manager.description();
		if(list.isEmpty()) {
			AlertWindow.warn("下载提示", "请选择下载文件");
			return;
		}
		entity.setDescription(JSONUtils.javaToJson(list));
		TaskRepository repository = new TaskRepository();
		repository.update(entity);
		DownloaderManager.getInstance().refresh(wrapper);
		EditWindow.getInstance().hide();
	};
	
}
