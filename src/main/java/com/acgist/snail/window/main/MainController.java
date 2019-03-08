package com.acgist.snail.window.main;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.context.SystemStatistical;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.window.about.AboutWindow;
import com.acgist.snail.window.alert.AlertWindow;
import com.acgist.snail.window.build.BuildWindow;
import com.acgist.snail.window.menu.TaskMenu;
import com.acgist.snail.window.setting.SettingWindow;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 * TODO：太多列滚动条优化
 * TODO：文件校验（MD5/SHA-1）
 * TODO：内存优化
 */
public class MainController implements Initializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
	
	/**
	 * 过滤
	 */
	public enum Filter {
		all,
		download,
		complete;
	}
	
	private Filter filter = Filter.all;
	
	@FXML
    private BorderPane root;
	@FXML
	private HBox header;
	@FXML
	private HBox footer;
	@FXML
	private HBox footerButton;
	@FXML
	private HBox footerStatus;
	@FXML
	private Label downloadBuffer;
	@FXML
	private TableView<TaskWrapper> taskTable;
	@FXML
	private TableColumn<TaskWrapper, String> name;
	@FXML
	private TableColumn<TaskWrapper, String> status;
	@FXML
	private TableColumn<TaskWrapper, String> progress;
	@FXML
	private TableColumn<TaskWrapper, String> createDate;
	@FXML
	private TableColumn<TaskWrapper, String> endDate;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 设置多选
		this.taskTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// 设置无数据内容
		ImageView noticeImage = new ImageView("/image/64/download_02.png");
		Text noticeText = new Text("点击新建按钮或者拖动种子文件开始下载");
		noticeText.setFill(Color.rgb(198, 198, 198));
		VBox placeholderBox = new VBox(noticeImage, noticeText);
		placeholderBox.setAlignment(Pos.CENTER);
		this.taskTable.setPlaceholder(placeholderBox);
		// 设置列
		taskCell(name, Pos.CENTER_LEFT, true, taskTable.widthProperty().divide(5D));
		taskCell(status, Pos.CENTER, false, taskTable.widthProperty().divide(10D));
		taskCell(progress, Pos.CENTER_LEFT, false, taskTable.widthProperty().divide(5D).subtract(20));
		taskCell(createDate, Pos.CENTER, false, taskTable.widthProperty().divide(4D));
		taskCell(endDate, Pos.CENTER, false, taskTable.widthProperty().divide(4D));
		// 设置行
		this.taskTable.setRowFactory(rowFactory);		
		// 绑定属性
		taskTable.prefWidthProperty().bind(root.widthProperty());
		taskTable.prefHeightProperty().bind(root.prefHeightProperty().subtract(80D));
		footerButton.prefWidthProperty().bind(root.widthProperty().multiply(0.5D));
		footerStatus.prefWidthProperty().bind(root.widthProperty().multiply(0.5D));
		// 文件拖拽
		taskTable.setOnDragOver(dragOverAction);
		taskTable.setOnDragDropped(dragDroppedAction);
		// 设置定时刷新
		TaskTimer.getInstance().newTimer(this);
	}

	@FXML
	public void handleBuildAction(ActionEvent event) {
		BuildWindow.getInstance().show();
	}
	
	@FXML
	public void handleStartAction(ActionEvent event) {
		this.start();
	}
	
	@FXML
	public void handlePauseAction(ActionEvent event) {
		this.pause();
	}
	
	@FXML
	public void handleDeleteAction(ActionEvent event) {
		this.delete();
	}
	
	@FXML
	public void handleAboutAction(ActionEvent event) {
		AboutWindow.getInstance().show();
	}
	
	@FXML
	public void handleSettingAction(ActionEvent event) {
		SettingWindow.getInstance().show();
	}
	
	@FXML
	public void handleAllAction(ActionEvent event) {
		this.filter = Filter.all;
		TaskTimer.getInstance().refreshTaskTable();
	}
	
	@FXML
	public void handleDownloadAction(ActionEvent event) {
		this.filter = Filter.download;
		TaskTimer.getInstance().refreshTaskTable();
	}
	
	@FXML
	public void handleCompleteAction(ActionEvent event) {
		this.filter = Filter.complete;
		TaskTimer.getInstance().refreshTaskTable();
	}
	
	/**
	 * 设置数据
	 */
	public void refreshTable() {
		ObservableList<TaskWrapper> obs = FXCollections.observableArrayList();
		DownloaderManager.getInstance().taskTable()
		.stream()
		.filter(wrapper -> {
			var status = wrapper.entity().getStatus();
			if(filter == Filter.all) {
				return true;
			} else if(filter == Filter.download) {
				return status == Status.await || status == Status.download;
			} else if(filter == Filter.complete) {
				return status == Status.complete;
			} else {
				return true;
			}
		})
		.forEach(wrapper -> {
			obs.add(wrapper);
		});
		taskTable.setItems(obs);
	}
	
	/**
	 * 刷新数据
	 */
	public void refreshData() {
		taskTable.refresh(); // 刷新table
		Platform.runLater(() -> {
			downloadBuffer.setText(SystemStatistical.getInstance().downloadBufferSecond()); // 下载速度
		});
	}
	
	/**
	 * 选中数据
	 */
	public List<TaskWrapper> selected() {
		return this.taskTable.getSelectionModel().getSelectedItems()
			.stream()
			.collect(Collectors.toList());
	}
	
	/**
	 * 是否选中数据
	 */
	public boolean hasContent() {
		return !this.selected().isEmpty();
	}
	
	/**
	 * 选中数据是否包含BT下载
	 */
	public boolean hasTorrent() {
		return this.selected()
			.stream()
			.anyMatch(wrapper -> wrapper.entity().getType() == Type.torrent);
	}

	/**
	 * 开始任务
	 */
	public void start() {
		this.selected()
		.forEach(wrapper -> {
			try {
				DownloaderManager.getInstance().start(wrapper);
			} catch (DownloadException e) {
				LOGGER.error("添加下载任务异常", e);
			}
		});
	}
	
	/**
	 * 暂停任务
	 */
	public void pause() {
		this.selected()
		.forEach(wrapper -> {
			DownloaderManager.getInstance().pause(wrapper);
		});
	}
	
	/**
	 * 删除任务
	 */
	public void delete() {
		if(!this.hasContent()) {
			return;
		}
		Optional<ButtonType> result = AlertWindow.build(AlertType.CONFIRMATION, "删除确认", "删除选中文件？");
		if(result.get() == ButtonType.OK) {
			this.selected()
			.forEach(wrapper -> {
				DownloaderManager.getInstance().delete(wrapper);
			});
			TaskTimer.getInstance().refreshTaskTable();
		}
	}

	/**
	 * 设置列
	 * @param column 列
	 * @param pos 对齐
	 * @param icon 显示ICON
	 * @param widthBinding 宽度绑定
	 */
	private void taskCell(TableColumn<TaskWrapper, String> column, Pos pos, boolean icon, DoubleBinding widthBinding) {
		column.prefWidthProperty().bind(widthBinding);
		column.setResizable(false);
		column.setCellFactory(new Callback<TableColumn<TaskWrapper, String>, TableCell<TaskWrapper, String>>() {
			@Override
			public TableCell<TaskWrapper, String> call(TableColumn<TaskWrapper, String> param) {
				return new TaskCell(pos, icon);
			}
		});
	}
	
	private Callback<TableView<TaskWrapper>, TableRow<TaskWrapper>> rowFactory = new Callback<TableView<TaskWrapper>, TableRow<TaskWrapper>>() {
		@Override
		public TableRow<TaskWrapper> call(TableView<TaskWrapper> param) {
			TableRow<TaskWrapper> row = new TableRow<>();
			row.setOnMouseClicked(rowClickAction); // 双击修改任务状态
			row.setContextMenu(TaskMenu.getInstance());
			return row;
		}
	};
	
	private EventHandler<DragEvent> dragOverAction = (event) -> {
		if (event.getGestureSource() != taskTable) {
			Dragboard dragboard = event.getDragboard();
			if(dragboard.hasFiles()) {
				File file = dragboard.getFiles().get(0);
				if(TorrentDecoder.verify(file.getPath())) {
					event.acceptTransferModes(TransferMode.COPY);
				} else {
					event.acceptTransferModes(TransferMode.NONE);
				}
			} else {
				event.acceptTransferModes(TransferMode.NONE);
			}
		}
		event.consume();
	};
	
	private EventHandler<DragEvent> dragDroppedAction = (event) -> {
		Dragboard dragboard = event.getDragboard();
		if (dragboard.hasFiles()) {
			File file = dragboard.getFiles().get(0);
			BuildWindow.getInstance().show(file.getPath());
		}
		event.setDropCompleted(true);
		event.consume();
	};
	
	@SuppressWarnings("unchecked")
	private EventHandler<MouseEvent> rowClickAction = (event) -> {
		if(event.getClickCount() == 2) { // 双击
			TableRow<TaskWrapper> row = (TableRow<TaskWrapper>) event.getSource();
			var wrapper = row.getItem();
			if(wrapper == null) {
				return;
			}
			if(wrapper.complete()) { // 下载完成=打开文件
				FileUtils.openInDesktop(new File(wrapper.entity().getFile()));
			} else if(wrapper.run()) { // 下载中=暂停下载
				DownloaderManager.getInstance().pause(wrapper);
			} else { // 其他=开始下载
				try {
					DownloaderManager.getInstance().start(wrapper);
				} catch (DownloadException e) {
					LOGGER.error("添加下载任务异常", e);
				}
			}
		}
	};
	
}
