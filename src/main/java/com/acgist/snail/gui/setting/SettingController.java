package com.acgist.snail.gui.setting;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.gui.Choosers;
import com.acgist.snail.gui.Controller;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.config.SystemConfig;
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
public final class SettingController extends Controller implements Initializable {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(SettingController.class);

	/**
	 * 大小滑块滑动大小：超过这个值时滑动必须是该值的整数倍
	 */
	private static final int STEP_WIDTH = 512;
	
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

	/**
	 * 下载目录
	 */
	@FXML
	public void handlePathAction(ActionEvent event) {
		final File file = Choosers.chooseDirectory(SettingWindow.getInstance().stage(), "下载保存目录");
		if (file != null) {
			final String path = file.getPath();
			DownloadConfig.setPath(path);
			this.pathValue.setText(DownloadConfig.getPath());
		}
	}

	/**
	 * 消息提示
	 */
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
		this.size.setOnMouseReleased(this.sizeReleaseAction);
		// 初始化下载速度设置
		this.buffer.valueProperty().addListener(this.bufferListener);
		this.buffer.setOnMouseReleased(this.bufferReleaseAction);
		this.buffer.setLabelFormatter(this.bufferFormatter);
		// 初始化下载磁盘缓存设置
		this.memoryBuffer.valueProperty().addListener(this.memoryBufferListener);
		this.memoryBuffer.setOnMouseReleased(this.memoryBufferReleaseAction);
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
	
	private EventHandler<MouseEvent> sizeReleaseAction = (event) -> {
		final Double value = this.size.getValue();
		DownloadConfig.setSize(value.intValue());
	};
	
	private ChangeListener<? super Number> bufferListener = (obs, oldVal, newVal) -> {
		int value = newVal.intValue();
		if(value > STEP_WIDTH) { // 超过512KB时设置为512KB整数倍
			value = value / STEP_WIDTH * STEP_WIDTH;
		} else if(value < SystemConfig.MIN_BUFFER_KB) { // 最小下载速度
			value = SystemConfig.MIN_BUFFER_KB;
		}
		this.buffer.setValue(value);
	};
	
	private EventHandler<MouseEvent> bufferReleaseAction = (event) -> {
		final Double value = this.buffer.getValue();
		DownloadConfig.setBuffer(value.intValue());
	};
	
	private StringConverter<Double> bufferFormatter = new StringConverter<Double>() {
		@Override
		public String toString(Double value) {
			return (value.intValue() / SystemConfig.DATA_SCALE) + "M";
		}
		@Override
		public Double fromString(String label) {
			return Double.valueOf(label.substring(0, label.length() - 1)) * SystemConfig.DATA_SCALE;
		}
	};
	
	private ChangeListener<? super Number> memoryBufferListener = (obs, oldVal, newVal) -> {
		final int value = newVal.intValue();
		this.memoryBuffer.setValue(value);
	};
	
	private EventHandler<MouseEvent> memoryBufferReleaseAction = (event) -> {
		final Double value = this.memoryBuffer.getValue();
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
