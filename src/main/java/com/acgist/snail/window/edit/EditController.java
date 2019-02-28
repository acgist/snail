package com.acgist.snail.window.edit;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;

public class EditController implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EditController.class);
	
	@FXML
    private FlowPane root;
	@FXML
	private TreeView<String> tree;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	/**
	 * 显示信息
	 */
	public void tree(TaskWrapper wrapper) {
		TorrentDecoder decoder = TorrentDecoder.newInstance(wrapper.getTorrent());
	}

}
