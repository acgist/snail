package com.acgist.snail.window.setting;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.FileUtils;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class SettingController implements Initializable {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(SettingController.class);

	@FXML
    private FlowPane root;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private Slider size;
	@FXML
	private Slider buffer;
	@FXML
	private Slider memoryBuffer;
	@FXML
	private Text pathValue;
	@FXML
	private CheckBox notice;
	@FXML
	private CheckBox p2p;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 绑定宽高
		scrollPane.prefWidthProperty().bind(root.widthProperty());
		scrollPane.prefHeightProperty().bind(root.heightProperty());
		// 初始化
		initSetting();
		initControl();
	}

	@FXML
	public void handlePathAction(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("文件保存目录");
		DownloadConfig.lastPath(chooser);
		File file = chooser.showDialog(new Stage());
		if (file != null) {
			DownloadConfig.setLastPath(file.getPath());
			String path = file.getPath();
			DownloadConfig.setPath(path);
			pathValue.setText(DownloadConfig.getPath());
		}
	}

	@FXML
	public void handleNoticeAction(ActionEvent event) {
		DownloadConfig.setNotice(notice.isSelected());
	}
	
	@FXML
	public void handleP2pAction(ActionEvent event) {
		DownloadConfig.setP2p(p2p.isSelected());
	}
	
	/**
	 * 初始化配置
	 */
	private void initSetting() {
		pathValue.setText(DownloadConfig.getPath());
		size.setValue(DownloadConfig.getSize());
		buffer.setValue(DownloadConfig.getBuffer());
		memoryBuffer.setValue(DownloadConfig.getMemoryBuffer());
		notice.setSelected(DownloadConfig.getNotice());
		p2p.setSelected(DownloadConfig.getP2p());
	}
	
	/**
	 * 初始化控件
	 */
	private void initControl() {
		// 初始化下载地址选择
		pathValue.setCursor(Cursor.HAND);
		pathValue.setOnMouseClicked(openDownloadPath);
		// 初始化下载大小设置
		size.valueProperty().addListener(sizeListener);
		size.setOnMouseReleased(sizeAction);
		// 初始化下载速度设置
		buffer.valueProperty().addListener(bufferListener);
		buffer.setOnMouseReleased(bufferAction);
		buffer.setLabelFormatter(bufferFormatter);
		// 初始化下载磁盘缓存设置
		memoryBuffer.valueProperty().addListener(memoryBufferListener);
		memoryBuffer.setOnMouseReleased(memoryBufferAction);
		memoryBuffer.setLabelFormatter(memoryBufferFormatter);
	}
	
	private EventHandler<MouseEvent> openDownloadPath = (event) -> {
		File open = new File(DownloadConfig.getPath());
		FileUtils.openInDesktop(open);
	};

	private ChangeListener<? super Number> sizeListener = (obs, oldVal, newVal) -> {
		int value = newVal.intValue(); // 设置整数个任务
		if(value == 0) { // 不能设置：0
			value = 1;
		}
		size.setValue(value);
	};
	
	private EventHandler<MouseEvent> sizeAction = (event) -> {
		Double value = size.getValue();
		DownloadConfig.setSize(value.intValue());
	};
	
	private ChangeListener<? super Number> bufferListener = (obs, oldVal, newVal) -> {
		int value = newVal.intValue();
		if(value > 512) { // 512KB以上时设置为512整数倍
			value = value / 512 * 512;
		} else if(value == 0) { // 不能设置：0
			value = 1;
		}
		buffer.setValue(value);
	};
	
	private EventHandler<MouseEvent> bufferAction = (event) -> {
		Double value = buffer.getValue();
		DownloadConfig.setBuffer(value.intValue());
	};
	
	private StringConverter<Double> bufferFormatter = new StringConverter<Double>() {
		@Override
		public String toString(Double value) {
			return (value.intValue() / 1024) + "M";
		}
		@Override
		public Double fromString(String label) {
			return Double.valueOf(label.substring(0, label.length() - 1)) * 1024;
		}
	};
	
	private ChangeListener<? super Number> memoryBufferListener = (obs, oldVal, newVal) -> {
		int value = newVal.intValue() / 8 * 8;
		memoryBuffer.setValue(value);
	};
	
	private EventHandler<MouseEvent> memoryBufferAction = (event) -> {
		Double value = memoryBuffer.getValue();
		DownloadConfig.setMemoryBuffer(value.intValue());
	};
	
	private StringConverter<Double> memoryBufferFormatter = new StringConverter<Double>() {
		@Override
		public String toString(Double value) {
			return value.intValue() + "M";
		}
		@Override
		public Double fromString(String label) {
			return Double.valueOf(label.substring(0, label.length() - 1));
		}
	};
	
}
