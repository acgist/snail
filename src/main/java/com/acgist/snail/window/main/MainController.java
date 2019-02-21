package com.acgist.snail.window.main;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.snail.pojo.message.DownloadMessage;
import com.acgist.snail.window.about.AboutWindow;
import com.acgist.snail.window.menu.TaskMenu;

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
	private TableView<DownloadMessage> downloadList;
	@FXML
	private TableColumn<DownloadMessage, String> name;
	@FXML
	private TableColumn<DownloadMessage, String> status;
	@FXML
	private TableColumn<DownloadMessage, String> progress;
	@FXML
	private TableColumn<DownloadMessage, String> begin;
	@FXML
	private TableColumn<DownloadMessage, String> end;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.downloadListMultiple();
		this.downloadListMapping();
		this.downloadListData();
		this.downloadListRow();
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
	
	private void downloadListMultiple() {
		this.downloadList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	
	private void downloadListMapping() {
		name.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("name"));
		status.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("status"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("progress"));
		begin.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("begin"));
		end.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("end"));
//		name();
	}
	
	private void downloadListData() {
		ObservableList<DownloadMessage> list = FXCollections.observableArrayList();
		for (int i = 0; i < 10; i++) {
			list.add(new DownloadMessage("测试" + i, "ces", "ces", "ces", "ces"));
		}
		downloadList.setItems(list);
	}
	
	private void downloadListRow() {
		MainController mainController = this;
		this.downloadList.setRowFactory(new Callback<TableView<DownloadMessage>, TableRow<DownloadMessage>>() {
			@Override
			public TableRow<DownloadMessage> call(TableView<DownloadMessage> param) {
				TableRow<DownloadMessage> row = new TableRow<>();
				row.setOnMouseClicked((event) -> {
					if(event.getClickCount() == 1) {
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
