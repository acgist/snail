package com.acgist.snail.gui.about;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.BrowseUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;

/**
 * 关于窗口控制器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class AboutController implements Initializable {
	
	@FXML
	private GridPane root;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	/**
	 * 作者按钮
	 */
	@FXML
	public void handleAuthorAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getAuthor());
	}
	
	/**
	 * 源码按钮
	 */
	@FXML
	public void handleSourceAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getSource());
	}
	
	/**
	 * 支持按钮
	 */
	@FXML
	public void handleSupportAction(ActionEvent event) {
		BrowseUtils.open(SystemConfig.getSupport());
	}

}
