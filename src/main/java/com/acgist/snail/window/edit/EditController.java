package com.acgist.snail.window.edit;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
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
		TreeItem<String> t = new TreeItem<String>("xxx");
		t.setExpanded(true);
		tree.setRoot(t);
		t.getChildren().addAll(new TreeItem<String>("1"), new TreeItem<String>("1"));
	}
	
}
