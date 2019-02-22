package com.acgist.snail.window.main;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.pojo.message.TaskMessage;
import com.acgist.snail.window.about.AboutWindow;
import com.acgist.snail.window.menu.TaskMenu;
import com.acgist.snail.window.setting.SettingWindow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

public class MainController implements Initializable {

	@FXML
    private BorderPane root;
	@FXML
	private TableView<TaskMessage> taskTable;
	@FXML
	private TableColumn<TaskMessage, String> name;
	@FXML
	private TableColumn<TaskMessage, String> status;
	@FXML
	private TableColumn<TaskMessage, String> progress;
	@FXML
	private TableColumn<TaskMessage, String> begin;
	@FXML
	private TableColumn<TaskMessage, String> end;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.taskTableColumnWidth();
		this.taskTableMultiple();
		this.taskTableMapping();
		this.taskTableData();
		this.taskTableRow();
	}

	@FXML
	public void handleBuildAction(ActionEvent event) {
	}
	
	@FXML
	public void handleStartAction(ActionEvent event) {
	}
	
	@FXML
	public void handlePauseAction(ActionEvent event) {
	}
	
	@FXML
	public void handleDeleteAction(ActionEvent event) {
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
	 * 设置列宽
	 */
	private void taskTableColumnWidth() {
		name.prefWidthProperty().bind(root.widthProperty().divide(5D));
		status.prefWidthProperty().bind(root.widthProperty().divide(10D));
		progress.prefWidthProperty().bind(root.widthProperty().divide(5D));
		begin.prefWidthProperty().bind(root.widthProperty().divide(4D));
		end.prefWidthProperty().bind(root.widthProperty().divide(4D));
		taskTable.prefWidthProperty().bind(root.widthProperty());
	}
	
	/**
	 * 设置多选
	 */
	private void taskTableMultiple() {
		this.taskTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	
	/**
	 * 数据映射
	 */
	private void taskTableMapping() {
		name.setResizable(false);
		name.setCellValueFactory(new PropertyValueFactory<TaskMessage, String>("name"));
		status.setResizable(false);
		status.setCellValueFactory(new PropertyValueFactory<TaskMessage, String>("status"));
		progress.setResizable(false);
		progress.setCellValueFactory(new PropertyValueFactory<TaskMessage, String>("progress"));
		begin.setResizable(false);
		begin.setCellValueFactory(new PropertyValueFactory<TaskMessage, String>("begin"));
		end.setResizable(false);
		end.setCellValueFactory(new PropertyValueFactory<TaskMessage, String>("end"));
//		name();
	}
	
	/**
	 * 设置数据
	 */
	private void taskTableData() {
		ObservableList<TaskMessage> list = FXCollections.observableArrayList();
		for (int i = 0; i < 100; i++) {
			list.add(new TaskMessage("测试" + i, "ces", "ces", "ces", "ces"));
		}
		taskTable.setItems(list);
	}
	
	/**
	 * 行事件
	 */
	private void taskTableRow() {
		MainController mainController = this;
		this.taskTable.setRowFactory(new Callback<TableView<TaskMessage>, TableRow<TaskMessage>>() {
			@Override
			public TableRow<TaskMessage> call(TableView<TaskMessage> param) {
				TableRow<TaskMessage> row = new TableRow<>();
				row.setOnMouseClicked((event) -> {
					if(event.getClickCount() == 2) {
						// TODO：暂停
					}
				});
				row.setContextMenu(TaskMenu.getInstance(mainController));
				return row;
			}
		});		
	}
	
//	private void name() {
//		name.setCellFactory(new Callback<TableColumn<DownloadMessage,String>, TableCell<DownloadMessage,String>>() {
//			@Override
//			public TableCell<DownloadMessage, String> call(TableColumn<DownloadMessage, String> param) {
//				TextFieldTableCell<DownloadMessage, String> cell = new TextFieldTableCell<>();
//				cell.setCursor(Cursor.HAND);
//				cell.setOnMouseClicked((event) -> {
//				});
//				return cell;
//			}
//		});
//	}
	
}
