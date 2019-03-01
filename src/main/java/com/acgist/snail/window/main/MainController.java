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

import javafx.beans.binding.DoubleBinding;
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
	private HBox footer;
	@FXML
	private HBox footerButton;
	@FXML
	private HBox footerStatus;
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
		// 设置列
		taskCell(name, Pos.CENTER_LEFT, true, "nameValue", taskTable.widthProperty().divide(5D));
		taskCell(status, Pos.CENTER, false, "statusValue", taskTable.widthProperty().divide(10D));
		taskCell(progress, Pos.CENTER_LEFT, false, "progressValue", taskTable.widthProperty().divide(5D).subtract(20));
		taskCell(createDate, Pos.CENTER, false, "createDateValue", taskTable.widthProperty().divide(4D));
		taskCell(endDate, Pos.CENTER, false, "endDateValue", taskTable.widthProperty().divide(4D));
		// 设置行
		this.taskTable.setRowFactory(rowFactory);		
		// 绑定属性
		taskTable.prefWidthProperty().bind(root.widthProperty());
		taskTable.prefHeightProperty().bind(root.prefHeightProperty().subtract(80D));
		footerButton.prefWidthProperty().bind(root.widthProperty().multiply(0.5D));
		footerStatus.prefWidthProperty().bind(root.widthProperty().multiply(0.5D));
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
	}
	
	@FXML
	public void handleDownloadingAction(ActionEvent event) {
	}
	
	@FXML
	public void handleCompleteAction(ActionEvent event) {
	}
	
	/**
	 * 设置数据
	 */
	public void taskTable(List<TaskWrapper> list) {
		ObservableList<TaskWrapper> obs = FXCollections.observableArrayList();
		list.forEach(wrapper -> {
			obs.add(wrapper);
		});
		taskTable.setItems(obs);
	}
	
	/**
	 * 刷新数据
	 */
	public void refresh() {
		taskTable.refresh();
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
			.anyMatch(wrapper -> wrapper.getType() == Type.torrent);
	}

	/**
	 * 开始任务
	 */
	public void start() {
		this.selected()
		.forEach(wrapper -> {
			DownloaderManager.getInstance().start(wrapper);
		});
		TaskTimer.getInstance().refreshTaskData();
	}
	
	/**
	 * 暂停任务
	 */
	public void pause() {
		this.selected()
		.forEach(wrapper -> {
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
	 * @param name 列名
	 * @param propertyBinding 属性绑定
	 * @param widthBinding 宽度绑定
	 */
	private void taskCell(TableColumn<TaskWrapper, String> column, Pos pos, boolean name, String propertyBinding, DoubleBinding widthBinding) {
		column.prefWidthProperty().bind(widthBinding);
		column.setResizable(false);
		column.setCellValueFactory(new PropertyValueFactory<TaskWrapper, String>(propertyBinding));
		column.setCellFactory(new Callback<TableColumn<TaskWrapper, String>, TableCell<TaskWrapper, String>>() {
			@Override
			public TableCell<TaskWrapper, String> call(TableColumn<TaskWrapper, String> param) {
				return new TaskCell(pos, name);
			}
		});
	}
	
	private Callback<TableView<TaskWrapper>, TableRow<TaskWrapper>> rowFactory = new Callback<TableView<TaskWrapper>, TableRow<TaskWrapper>>() {
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
	};
	
}
