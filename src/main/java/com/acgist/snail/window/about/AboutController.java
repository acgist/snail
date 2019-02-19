package com.acgist.snail.window.about;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.context.SpringContextUtils;
import com.acgist.snail.pojo.config.SystemConfig;
import com.acgist.snail.utils.BrowseUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * 关于
 */
public class AboutController implements Initializable {
	
	private SystemConfig systemConfig;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		systemConfig = SpringContextUtils.getBean(SystemConfig.class);
	}
	
	@FXML
	public void handleAuthorAction(ActionEvent event) {
		BrowseUtils.open(systemConfig.getAuthor());
	}
	
	@FXML
	public void handleSourceAction(ActionEvent event) {
		BrowseUtils.open(systemConfig.getSource());
	}
	
	@FXML
	public void handleSupportAction(ActionEvent event) {
		BrowseUtils.open(systemConfig.getSupport());
	}

}
