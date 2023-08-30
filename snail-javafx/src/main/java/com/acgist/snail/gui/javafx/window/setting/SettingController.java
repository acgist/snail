package com.acgist.snail.gui.javafx.window.setting;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.gui.javafx.Choosers;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.Tooltips;
import com.acgist.snail.gui.javafx.window.Controller;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;

/**
 * 设置窗口控制器
 * 
 * @author acgist
 */
public final class SettingController extends Controller {
    
    /**
     * 下载速度滑块滑动大小：{@value}
     */
    private static final int MIN_STEP_SIZE = 512;
    
    @FXML
    private FlowPane root;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label pathValue;
    @FXML
    private Slider size;
    @FXML
    private Slider buffer;
    @FXML
    private Slider memoryBuffer;
    @FXML
    private CheckBox notice;
    @FXML
    private CheckBox delete;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 绑定宽高
        this.scrollPane.prefWidthProperty().bind(this.root.widthProperty());
        this.scrollPane.prefHeightProperty().bind(this.root.heightProperty());
        // 初始化
        this.initSetting();
        this.initControl();
    }

    /**
     * 下载目录
     * 
     * @param event 事件
     */
    @FXML
    public void handlePathAction(ActionEvent event) {
        final File file = Choosers.chooseDirectory(SettingWindow.getInstance().getStage(), "下载目录");
        if (file != null) {
            final String path = file.getAbsolutePath();
            DownloadConfig.setPath(path);
            this.pathValue(DownloadConfig.getPath());
        }
    }

    /**
     * 消息提示
     * 
     * @param event 事件
     */
    @FXML
    public void handleNoticeAction(ActionEvent event) {
        DownloadConfig.setNotice(this.notice.isSelected());
    }
    
    /**
     * 删除文件
     * 
     * @param event 事件
     */
    @FXML
    public void handleDeleteAction(ActionEvent event) {
        DownloadConfig.setDelete(this.delete.isSelected());
    }
    
    /**
     * 初始化配置
     */
    private void initSetting() {
        this.pathValue(DownloadConfig.getPath());
        this.size.setValue(DownloadConfig.getSize());
        this.buffer.setValue(DownloadConfig.getBuffer());
        this.memoryBuffer.setValue(DownloadConfig.getMemoryBuffer());
        this.notice.setSelected(DownloadConfig.getNotice());
        this.delete.setSelected(DownloadConfig.getDelete());
    }
    
    /**
     * 初始化控件
     */
    private void initControl() {
        // 初始化下载目录
        this.pathValue.setCursor(Cursor.HAND);
        this.pathValue.setOnMouseReleased(this.pathReleaseAction);
        // 初始化任务数量
        this.size.valueProperty().addListener(this.sizeListener);
        this.size.setOnMouseReleased(this.sizeReleaseAction);
        // 初始化下载速度
        this.buffer.valueProperty().addListener(this.bufferListener);
        this.buffer.setOnMouseReleased(this.bufferReleaseAction);
        this.buffer.setLabelFormatter(this.bufferFormatter);
        // 初始化磁盘缓存
        this.memoryBuffer.valueProperty().addListener(this.memoryBufferListener);
        this.memoryBuffer.setOnMouseReleased(this.memoryBufferReleaseAction);
        this.memoryBuffer.setLabelFormatter(this.memoryBufferFormatter);
    }
    
    /**
     * 下载目录点击事件
     */
    private EventHandler<MouseEvent> pathReleaseAction = event -> Desktops.open(new File(DownloadConfig.getPath()));

    /**
     * 下载任务数量监听
     */
    private ChangeListener<? super Number> sizeListener = (obs, oldVal, newVal) -> {
        // 设置整数任务
        final int value = newVal.intValue();
        // 至少下载一个任务
        this.size.setValue(value <= 0 ? 1 : value);
    };
    
    /**
     * 下载任务数量保存
     */
    private EventHandler<MouseEvent> sizeReleaseAction = event -> {
        final Double value = this.size.getValue();
        DownloadConfig.setSize(value.intValue());
    };
    
    /**
     * 下载速度监听
     */
    private ChangeListener<? super Number> bufferListener = (obs, oldVal, newVal) -> {
        int value = newVal.intValue();
        if(value < SystemConfig.MIN_DOWNLOAD_BUFFER_KB) {
            // 最小下载速度
            value = SystemConfig.MIN_DOWNLOAD_BUFFER_KB;
        } else if(value > MIN_STEP_SIZE) {
            // 超过滑块大小时设置为滑块大小的整数倍
            value = value / MIN_STEP_SIZE * MIN_STEP_SIZE;
        } else {
            // 其他情况
        }
        this.buffer.setValue(value);
    };
    
    /**
     * 下载速度保存
     */
    private EventHandler<MouseEvent> bufferReleaseAction = event -> {
        final Double value = this.buffer.getValue();
        DownloadConfig.setBuffer(value.intValue());
    };
    
    /**
     * 下载速度格式
     */
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
    
    /**
     * 磁盘缓存监听
     */
    private ChangeListener<? super Number> memoryBufferListener = (obs, oldVal, newVal) -> {
        final int value = newVal.intValue();
        this.memoryBuffer.setValue(value);
    };
    
    /**
     * 磁盘缓存保存
     */
    private EventHandler<MouseEvent> memoryBufferReleaseAction = event -> {
        final Double value = this.memoryBuffer.getValue();
        DownloadConfig.setMemoryBuffer(value.intValue());
    };
    
    /**
     * 磁盘缓存格式
     */
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
    
    /**
     * 设置下载路径
     * 
     * @param path 下载路径
     */
    private void pathValue(String path) {
        this.pathValue.setText(path);
        this.pathValue.setTooltip(Tooltips.newTooltip(path));
    }
    
}
