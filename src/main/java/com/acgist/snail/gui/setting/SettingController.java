package com.acgist.snail.gui.setting;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.gui.Choosers;
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
import javafx.util.StringConverter;

/**
 * 设置窗口控制器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class SettingController implements Initializable {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(SettingController.class);

	@FXML
    private FlowPane root;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private Text pathValue;
	@FXML
	private Slider size;
	@FXML
	private Slider buffer;
	@FXML
	private Slider memoryBuffer;
	@FXML
	private CheckBox notice;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 绑定宽高
		this.scrollPane.prefWidthProperty().bind(this.root.widthProperty());
		this.scrollPane.prefHeightProperty().bind(this.root.heightProperty());
		// 初始化
		initSetting();
		initControl();
	}

	@FXML
	public void handlePathAction(ActionEvent event) {
		final File file = Choosers.chooseDirectory(SettingWindow.getInstance().stage(), "文件保存目录");
		if (file != null) {
			final String path = file.getPath();
			DownloadConfig.setPath(path);
			this.pathValue.setText(DownloadConfig.getPath());
		}
	}

	@FXML
	public void handleNoticeAction(ActionEvent event) {
		DownloadConfig.setNotice(this.notice.isSelected());
	}
	
	/**
	 * 初始化配置
	 */
	private void initSetting() {
		this.pathValue.setText(DownloadConfig.getPath());
		this.size.setValue(DownloadConfig.getSize());
		this.buffer.setValue(DownloadConfig.getBuffer());
		this.memoryBuffer.setValue(DownloadConfig.getMemoryBuffer());
		this.notice.setSelected(DownloadConfig.getNotice());
	}
	
	/**
	 * 初始化控件
	 */
	private void initControl() {
		// 初始化下载地址选择
		this.pathValue.setCursor(Cursor.HAND);
		this.pathValue.setOnMouseClicked(this.openDownloadPath);
		// 初始化下载大小设置
		this.size.valueProperty().addListener(this.sizeListener);
		this.size.setOnMouseReleased(this.sizeAction);
		// 初始化下载速度设置
		this.buffer.valueProperty().addListener(this.bufferListener);
		this.buffer.setOnMouseReleased(this.bufferAction);
		this.buffer.setLabelFormatter(this.bufferFormatter);
		// 初始化下载磁盘缓存设置
		this.memoryBuffer.valueProperty().addListener(this.memoryBufferListener);
		this.memoryBuffer.setOnMouseReleased(this.memoryBufferAction);
		this.memoryBuffer.setLabelFormatter(this.memoryBufferFormatter);
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
		this.size.setValue(value);
	};
	
	private EventHandler<MouseEvent> sizeAction = (event) -> {
		Double value = this.size.getValue();
		DownloadConfig.setSize(value.intValue());
	};
	
	private ChangeListener<? super Number> bufferListener = (obs, oldVal, newVal) -> {
		int value = newVal.intValue();
		if(value > 512) { // 512KB以上时设置为512整数倍
			value = value / 512 * 512;
		} else if(value == 0) { // 不能设置：0
			value = 1;
		}
		this.buffer.setValue(value);
	};
	
	private EventHandler<MouseEvent> bufferAction = (event) -> {
		Double value = this.buffer.getValue();
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
		this.memoryBuffer.setValue(value);
	};
	
	private EventHandler<MouseEvent> memoryBufferAction = (event) -> {
		Double value = this.memoryBuffer.getValue();
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
