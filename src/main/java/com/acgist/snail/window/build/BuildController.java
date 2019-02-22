package com.acgist.snail.window.build;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class BuildController implements Initializable {
	
	@FXML
    private FlowPane root;
	@FXML
	private TextField pathValue;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	@FXML
	public void handleTorrentAction(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("选择种子文件");
//		chooser.setSelectedExtensionFilter(new ExtensionFilter("种子文件", "*.torrent")); // 无效写法
		chooser.getExtensionFilters().add(new ExtensionFilter("种子文件", "*.torrent"));
		File file = chooser.showOpenDialog(new Stage());
		if (file != null) {
			pathValue.setText(file.getPath());
		}
	}

	@FXML
	public void handleBuildAction(ActionEvent event) {
		String value = pathValue.getText();
		if(StringUtils.isEmpty(value)) {
			return;
		}
		System.out.println(value);
	}

	@FXML
	public void handleCancelAction(ActionEvent event) {
		BuildWindow.getInstance().hide();
	}
	
}
