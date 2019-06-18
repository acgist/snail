package com.acgist.snail.gui.main;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Alerts;
import com.acgist.snail.gui.about.AboutWindow;
import com.acgist.snail.gui.build.BuildWindow;
import com.acgist.snail.gui.menu.TaskMenu;
import com.acgist.snail.gui.setting.SettingWindow;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.DownloaderManager;
import com.acgist.snail.system.manager.ProtocolManager;
import com.acgist.snail.system.statistics.SystemStatistics;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

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
 * 主窗口控制器
 * TODO：太多列滚动条优化
 * TODO：内存优化
 * TODO：下键空白
 * 
 * @author acgist
 * @since 1.0.0
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
	
	/**
	 * 显示列表过滤器
	 */
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
	private Label uploadBuffer;
	@FXML
	private TableView<TaskSession> taskTable;
	@FXML
	private TableColumn<TaskSession, String> name;
	@FXML
	private TableColumn<TaskSession, String> status;
	@FXML
	private TableColumn<TaskSession, String> progress;
	@FXML
	private TableColumn<TaskSession, String> createDate;
	@FXML
	private TableColumn<TaskSession, String> endDate;

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
		taskCell(this.name, Pos.CENTER_LEFT, true, this.taskTable.widthProperty().divide(5D));
		taskCell(this.status, Pos.CENTER, false, this.taskTable.widthProperty().divide(10D));
		taskCell(this.progress, Pos.CENTER_LEFT, false, this.taskTable.widthProperty().divide(5D).subtract(20));
		taskCell(this.createDate, Pos.CENTER, false, this.taskTable.widthProperty().divide(4D));
		taskCell(this.endDate, Pos.CENTER, false, this.taskTable.widthProperty().divide(4D));
		// 设置行
		this.taskTable.setRowFactory(this.rowFactory);
		// 绑定属性
		this.taskTable.prefWidthProperty().bind(this.root.widthProperty());
		this.taskTable.prefHeightProperty().bind(this.root.prefHeightProperty().subtract(80D));
		this.footerButton.prefWidthProperty().bind(this.root.widthProperty().multiply(0.5D));
		this.footerStatus.prefWidthProperty().bind(this.root.widthProperty().multiply(0.5D));
		// 文件拖拽
		this.taskTable.setOnDragOver(this.dragOverAction);
		this.taskTable.setOnDragDropped(this.dragDroppedAction);
		// 设置定时刷新
		TaskDisplay.getInstance().newTimer(this);
	}

	/**
	 * 新建按钮
	 */
	@FXML
	public void handleBuildAction(ActionEvent event) {
		BuildWindow.getInstance().show();
	}

	/**
	 * 开始按钮
	 */
	@FXML
	public void handleStartAction(ActionEvent event) {
		this.start();
	}

	/**
	 * 暂停按钮
	 */
	@FXML
	public void handlePauseAction(ActionEvent event) {
		this.pause();
	}
	
	/**
	 * 删除按钮
	 */
	@FXML
	public void handleDeleteAction(ActionEvent event) {
		this.delete();
	}
	
	/**
	 * 关于按钮
	 */
	@FXML
	public void handleAboutAction(ActionEvent event) {
		AboutWindow.getInstance().show();
	}
	
	/**
	 * 设置按钮
	 */
	@FXML
	public void handleSettingAction(ActionEvent event) {
		SettingWindow.getInstance().show();
	}
	
	/**
	 * 全部任务按钮
	 */
	@FXML
	public void handleAllAction(ActionEvent event) {
		this.filter = Filter.all;
		TaskDisplay.getInstance().refreshTaskTable();
	}

	/**
	 * 下载中任务按钮
	 */
	@FXML
	public void handleDownloadAction(ActionEvent event) {
		this.filter = Filter.download;
		TaskDisplay.getInstance().refreshTaskTable();
	}
	
	/**
	 * 下载完成任务按钮
	 */
	@FXML
	public void handleCompleteAction(ActionEvent event) {
		this.filter = Filter.complete;
		TaskDisplay.getInstance().refreshTaskTable();
	}
	
	/**
	 * 刷新Table
	 */
	public void refreshTable() {
		final ObservableList<TaskSession> obs = FXCollections.observableArrayList();
		DownloaderManager.getInstance().tasks()
		.stream()
		.filter(wrapper -> {
			var status = wrapper.entity().getStatus();
			if(this.filter == Filter.all) {
				return true;
			} else if(this.filter == Filter.download) {
				return status == Status.await || status == Status.download;
			} else if(this.filter == Filter.complete) {
				return status == Status.complete;
			} else {
				return true;
			}
		})
		.forEach(wrapper -> {
			obs.add(wrapper);
		});
		this.taskTable.setItems(obs);
	}
	
	/**
	 * 刷新数据
	 */
	public void refreshData() {
		this.taskTable.refresh(); // 刷新table
		Platform.runLater(() -> {
			long downloadSecond = SystemStatistics.getInstance().downloadSecond();
			this.downloadBuffer.setText(FileUtils.formatSize(downloadSecond) + "/S"); // 下载速度
			long uploadSecond = SystemStatistics.getInstance().uploadSecond();
			this.uploadBuffer.setText(FileUtils.formatSize(uploadSecond) + "/S"); // 上传速度
		});
	}
	
	/**
	 * 获取选中任务
	 */
	public List<TaskSession> selected() {
		return this.taskTable.getSelectionModel().getSelectedItems()
			.stream()
			.collect(Collectors.toList());
	}
	
	/**
	 * 是否有选中任务
	 */
	public boolean haveContent() {
		return !this.selected().isEmpty();
	}
	
	/**
	 * 选中任务是否包含BT下载
	 */
	public boolean haveTorrent() {
		return this.selected()
			.stream()
			.anyMatch(wrapper -> wrapper.entity().getType() == Type.torrent);
	}

	/**
	 * 开始选中任务
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
	 * 暂停选中任务
	 */
	public void pause() {
		this.selected()
		.forEach(wrapper -> {
			DownloaderManager.getInstance().pause(wrapper);
		});
	}
	
	/**
	 * 删除选中任务
	 */
	public void delete() {
		if(!this.haveContent()) {
			return;
		}
		final Optional<ButtonType> result = Alerts.build(AlertType.CONFIRMATION, "删除确认", "删除选中文件（不可恢复）？");
		if(result.get() == ButtonType.OK) {
			this.selected()
			.forEach(wrapper -> {
				DownloaderManager.getInstance().delete(wrapper);
			});
			TaskDisplay.getInstance().refreshTaskTable();
		}
	}

	/**
	 * 设置数据列
	 * 
	 * @param column 列
	 * @param pos 对齐
	 * @param icon 显示ICON
	 * @param widthBinding 宽度绑定
	 */
	private void taskCell(TableColumn<TaskSession, String> column, Pos pos, boolean icon, DoubleBinding widthBinding) {
		column.prefWidthProperty().bind(widthBinding);
		column.setResizable(false);
		column.setCellFactory(new Callback<TableColumn<TaskSession, String>, TableCell<TaskSession, String>>() {
			@Override
			public TableCell<TaskSession, String> call(TableColumn<TaskSession, String> param) {
				return new TaskCell(pos, icon);
			}
		});
	}
	
	private Callback<TableView<TaskSession>, TableRow<TaskSession>> rowFactory = new Callback<TableView<TaskSession>, TableRow<TaskSession>>() {
		@Override
		public TableRow<TaskSession> call(TableView<TaskSession> param) {
			final TableRow<TaskSession> row = new TableRow<>();
			row.setOnMouseClicked(rowClickAction); // 双击修改任务状态
			row.setContextMenu(TaskMenu.getInstance());
			return row;
		}
	};
	
	/**
	 * 拖入文件事件（显示）
	 */
	private EventHandler<DragEvent> dragOverAction = (event) -> {
		if (event.getGestureSource() != this.taskTable) {
			String url = null;
			final Dragboard dragboard = event.getDragboard();
			if(dragboard.hasFiles()) {
				final File file = dragboard.getFiles().get(0);
				url = file.getPath();
			} else if(dragboard.hasUrl()) {
				url = dragboard.getUrl();
			} else if(dragboard.hasString()) {
				url = dragboard.getString();
			}
			if(ProtocolManager.getInstance().support(url)) {
				event.acceptTransferModes(TransferMode.COPY);
			} else {
				event.acceptTransferModes(TransferMode.NONE);
			}
		}
		event.consume();
	};
	
	/**
	 * 拖入文件事件（加载）
	 */
	private EventHandler<DragEvent> dragDroppedAction = (event) -> {
		String url = null;
		final Dragboard dragboard = event.getDragboard();
		if (dragboard.hasFiles()) {
			final File file = dragboard.getFiles().get(0);
			url = file.getPath();
		} else if(dragboard.hasUrl()) {
			url = dragboard.getUrl();
		} else if(dragboard.hasString()) {
			url = dragboard.getString();
		}
		if(StringUtils.isNotEmpty(url)) {
			BuildWindow.getInstance().show(url);
		}
		event.setDropCompleted(true);
		event.consume();
	};
	
	/**
	 * 列双击事件
	 */
	private EventHandler<MouseEvent> rowClickAction = (event) -> {
		if(event.getClickCount() == 2) { // 双击
			final TableRow<?> row = (TableRow<?>) event.getSource();
			TaskSession session = (TaskSession) row.getItem();
			if(session == null) {
				return;
			}
			if(session.complete()) { // 下载完成=打开文件
				FileUtils.openInDesktop(new File(session.entity().getFile()));
			} else if(session.coming()) { // 准备中=暂停下载
				DownloaderManager.getInstance().pause(session);
			} else { // 其他=开始下载
				try {
					DownloaderManager.getInstance().start(session);
				} catch (DownloadException e) {
					LOGGER.error("开始下载任务异常", e);
				}
			}
		}
	};
	
}
