package com.acgist.snail.window.setting;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.service.ConfigService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class SettingController implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SettingController.class);

	@FXML
    private FlowPane root;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private Slider size;
	@FXML
	private Slider buffer;
	@FXML
	private Text pathValue;
	@FXML
	private CheckBox notice;
	@FXML
	private CheckBox p2p;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initScrollPane();
		initPathValue();
		initSlider();
		initSetting();
	}

	@FXML
	public void handlePathAction(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File file = chooser.showDialog(new Stage());
		if (file != null) {
			String path = file.getPath();
			ConfigService.getInstance().setDownloadPath(path);
			pathValue.setText(ConfigService.getInstance().getDownloadPath());
		}
	}

	@FXML
	public void handleNoticeAction(ActionEvent event) {
		ConfigService.getInstance().setDownloadNotice(notice.isSelected());
	}
	
	@FXML
	public void handleP2pAction(ActionEvent event) {
		ConfigService.getInstance().setDownloadP2p(p2p.isSelected());
	}
	
	private void initScrollPane() {
//		scrollPane.prefWidthProperty().bind(root.widthProperty());
		scrollPane.prefHeightProperty().bind(root.heightProperty());
	}
	
	/**
	 * 初始化配置项
	 */
	private void initSetting() {
		ConfigService configService = ConfigService.getInstance();
		pathValue.setText(configService.getDownloadPath());
		size.setValue(configService.getDownloadSize());
		buffer.setValue(configService.getDownloadBuffer());
		notice.setSelected(configService.getDownloadNotice());
		p2p.setSelected(configService.getDownloadP2p());
	}
	
	/**
	 * 初始化下载目录显示
	 */
	private void initPathValue() {
		pathValue.setCursor(Cursor.HAND);
		pathValue.setOnMouseClicked((event) -> {
			File open = new File(ConfigService.getInstance().getDownloadPath());
			try {
				Desktop.getDesktop().open(open);
			} catch (IOException e) {
				LOGGER.error("打开系统目录异常", e);
			}
		});
	}
	
	/**
	 * 初始化滑动选择项
	 */
	private void initSlider() {
		size.valueProperty().addListener((obs, oldval, newVal) -> {
			size.setValue(newVal.intValue()); // 设置整数个任务
		});
		size.setOnMouseReleased((event) -> {
			Double value = size.getValue();
			ConfigService.getInstance().setDownloadSize(value.intValue());
		});
		buffer.valueProperty().addListener((obs, oldval, newVal) -> {
			int value = newVal.intValue();
			if(value > 512) { // 512KB以上时设置为512整数倍
				value = value / 512 * 512;
			}
			buffer.setValue(value);
		});
		buffer.setOnMouseReleased((event) -> {
			Double value = buffer.getValue();
			ConfigService.getInstance().setDownloadBuffer(value.intValue());
		});
		buffer.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(Double value) {
				return (value / 1024) + "M";
			}
			@Override
			public Double fromString(String label) {
				return Double.valueOf(label.substring(0, label.length() - 1)) * 1024;
			}
		});
	}

}
