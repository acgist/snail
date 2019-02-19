package com.acgist.killer.window.main;

import java.net.URL;
import java.util.ResourceBundle;

import com.acgist.killer.pojo.message.DownloadMessage;
import com.acgist.killer.window.about.AboutWindow;
import com.acgist.killer.window.menu.TaskMenu;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class MainController implements Initializable {

	@FXML
    private BorderPane root;
	@FXML
	public TableView<DownloadMessage> downloadList;
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
		this.downloadListMapping();
		this.downloadListData();
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
		AboutWindow.show();
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
	
	private void downloadListMapping() {
		name.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("name"));
		status.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("status"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("progress"));
		begin.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("begin"));
		end.setCellValueFactory(new PropertyValueFactory<DownloadMessage, String>("end"));
	}
	
	private void downloadListData() {
		ObservableList<DownloadMessage> list = FXCollections.observableArrayList();
		for (int i = 0; i < 10; i++) {
			list.add(new DownloadMessage("测试" + i, "ces", "ces", "ces", "ces"));
		}
		downloadList.setItems(list);
		downloadList.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(event.getButton() == MouseButton.SECONDARY) {
					TaskMenu.getInstance().show(root, event.getSceneX(), event.getSceneY());
				}
			}
		});
	}
	
}
