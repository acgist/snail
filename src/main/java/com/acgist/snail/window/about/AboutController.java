package com.acgist.snail.window.about;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.module.config.SystemConfig;
import com.acgist.snail.utils.BrowseUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * 关于
 */
public class AboutController implements Initializable {
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	@FXML
	public void handleAuthorAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getAuthor());
	}
	
	@FXML
	public void handleSourceAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getSource());
	}
	
	@FXML
	public void handleSupportAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getSupport());
	}

}
