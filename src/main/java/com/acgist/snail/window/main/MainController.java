package com.acgist.snail.window.main;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.window.AlertWindow;
import com.acgist.snail.window.about.AboutWindow;
import com.acgist.snail.window.build.BuildWindow;
import com.acgist.snail.window.menu.TaskMenu;
import com.acgist.snail.window.setting.SettingWindow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * TODO：太多列滚动条优化
 */
public class MainController implements Initializable {

	@FXML
    private BorderPane root;
	@FXML
	private HBox header;
	@FXML
	private HBox fooder;
	@FXML
	private HBox fooderButton;
	@FXML
	private HBox fooderStatus;
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
		this.taskTableMultiple();
		this.taskTableColumn();
		this.taskTableRow();
		this.initView();
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
	}
	
	@FXML
	public void handleDownloadingAction(ActionEvent event) {
	}
	
	@FXML
	public void handleCompleteAction(ActionEvent event) {
	}
	
	/**
	 * 设置多选
	 */
	private void taskTableMultiple() {
		this.taskTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	/**
	 * 设置列
	 */
	private void taskTableColumn() {
		name.prefWidthProperty().bind(taskTable.widthProperty().divide(5D));
		status.prefWidthProperty().bind(taskTable.widthProperty().divide(10D));
		progress.prefWidthProperty().bind(taskTable.widthProperty().divide(5D).subtract(20));
		createDate.prefWidthProperty().bind(taskTable.widthProperty().divide(4D));
		endDate.prefWidthProperty().bind(taskTable.widthProperty().divide(4D));
		name.setResizable(false);
		name.setCellValueFactory(new PropertyValueFactory<TaskWrapper, String>("nameValue"));
		taskCell(name, Pos.CENTER_LEFT, true);
		status.setResizable(false);
		status.setCellValueFactory(new PropertyValueFactory<TaskWrapper, String>("statusValue"));
		taskCell(status, Pos.CENTER, false);
		progress.setResizable(false);
		progress.setCellValueFactory(new PropertyValueFactory<TaskWrapper, String>("progressValue"));
		taskCell(progress, Pos.CENTER_LEFT, false);
		createDate.setResizable(false);
		createDate.setCellValueFactory(new PropertyValueFactory<TaskWrapper, String>("createDateValue"));
		taskCell(createDate, Pos.CENTER, false);
		endDate.setResizable(false);
		endDate.setCellValueFactory(new PropertyValueFactory<TaskWrapper, String>("endDateValue"));
		taskCell(endDate, Pos.CENTER, false);
	}
	
	/**
	 * 设置行
	 */
	private void taskTableRow() {
		this.taskTable.setRowFactory(new Callback<TableView<TaskWrapper>, TableRow<TaskWrapper>>() {
			@Override
			public TableRow<TaskWrapper> call(TableView<TaskWrapper> param) {
				TableRow<TaskWrapper> row = new TableRow<>();
				row.setOnMouseClicked((event) -> {
					if(event.getClickCount() == 2) {
						// TODO：暂停
					}
				});
				row.setContextMenu(TaskMenu.getInstance());
				return row;
			}
		});		
	}
	
	/**
	 * 设置列
	 */
	private void taskCell(TableColumn<TaskWrapper, String> column, Pos pos, boolean name) {
		column.setCellFactory(new Callback<TableColumn<TaskWrapper, String>, TableCell<TaskWrapper, String>>() {
			@Override
			public TableCell<TaskWrapper, String> call(TableColumn<TaskWrapper, String> param) {
				return new TaskCell(pos, name);
//				return new TextFieldTableCell<>();
			}
		});
	}

	/**
	 * 初始控件
	 */
	private void initView() {
		taskTable.prefWidthProperty().bind(root.widthProperty());
		taskTable.prefHeightProperty().bind(root.prefHeightProperty().subtract(80D));
		fooderButton.prefWidthProperty().bind(root.widthProperty().multiply(0.5D));
		fooderStatus.prefWidthProperty().bind(root.widthProperty().multiply(0.5D));
		TaskTimer.getInstance().newTimer(this);
	}
	
	/**
	 * 设置数据
	 */
	public void setTaskTable(List<TaskWrapper> list) {
		ObservableList<TaskWrapper> obs = FXCollections.observableArrayList();
		list.forEach(wrapper -> {
			obs.add(wrapper);
		});
		taskTable.setItems(obs);
	}
	
	/**
	 * 设置数据
	 */
	public void refresh() {
		taskTable.refresh();
	}
	
	/**
	 * 获取被选中的信息
	 */
	public List<TaskWrapper> selected() {
		return this.taskTable.getSelectionModel().getSelectedItems()
			.stream()
			.collect(Collectors.toList());
	}
	
	/**
	 * 是否选中
	 */
	public boolean hasContent() {
		return !selected().isEmpty();
	}
	
	/**
	 * 选中内容是否包含BT下载
	 */
	public boolean hasTorrent() {
		return selected()
			.stream()
			.anyMatch(wrapper -> wrapper.getType() == Type.torrent);
	}

	/**
	 * 开始任务
	 */
	public void start() {
		List<TaskWrapper> list = this.selected();
		list.forEach(wrapper -> {
			DownloaderManager.getInstance().start(wrapper);
		});
		TaskTimer.getInstance().refreshTaskData();
	}
	
	/**
	 * 暂停任务
	 */
	public void pause() {
		List<TaskWrapper> list = this.selected();
		list.forEach(wrapper -> {
			DownloaderManager.getInstance().pause(wrapper);
		});
		TaskTimer.getInstance().refreshTaskData();
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
			List<TaskWrapper> list = this.selected();
			list.forEach(wrapper -> {
				DownloaderManager.getInstance().delete(wrapper);
			});
			TaskTimer.getInstance().refreshTaskTable();
		}
	}

}
