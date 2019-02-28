package com.acgist.snail.window.edit;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.coder.torrent.TorrentFiles;
import com.acgist.snail.coder.torrent.TorrentInfo;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class EditController implements Initializable {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(EditController.class);
	
	private static final String HIDE_FILE_PREFIX = "_____padding_file"; // 不需要下载的文件前缀
	
	@FXML
    private FlowPane root;
	@FXML
	private TreeView<HBox> tree;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tree.prefWidthProperty().bind(root.widthProperty());
		tree.prefHeightProperty().bind(root.heightProperty());
	}

	/**
	 * 显示信息
	 */
	public void tree(TaskWrapper wrapper) {
		TorrentDecoder decoder = TorrentDecoder.newInstance(wrapper.getTorrent());
		TorrentInfo info = decoder.torrentInfo();
		TorrentFiles files = info.getInfo();
		FileSelectManager manager = new FileSelectManager(files.getName(), tree);
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
	
}
