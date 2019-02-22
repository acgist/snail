package com.acgist.snail.window.setting;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;

public class SettingController implements Initializable {

	@FXML
    private FlowPane root;
	@FXML
	private ScrollPane scrollPane;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
//		scrollPane.prefWidthProperty().bind(root.widthProperty());
		scrollPane.prefHeightProperty().bind(root.heightProperty());
	}

}
