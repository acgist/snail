package com.acgist.snail.window.menu;

import com.acgist.snail.window.main.MainController;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

/**
 * 任务按钮
 * @author 28954
 *
 */
public class TaskMenu extends ContextMenu {

	private MainController mainController;
	
	private static TaskMenu INSTANCE;
	
	private TaskMenu() {
		createMenu();
	}
	
	public static final TaskMenu getInstance(MainController mainController) {
		if (INSTANCE == null) {
			synchronized (TaskMenu.class) {
				INSTANCE = new TaskMenu();
			}
		}
		INSTANCE.mainController = mainController;
		return INSTANCE;
	}

	/**
	 * 创建菜单
	 */
	private void createMenu() {
		MenuItem startMenu = new MenuItem("开始", new ImageView("/image/16/start.png"));
		MenuItem pauseMenu = new MenuItem("暂停", new ImageView("/image/16/pause.png"));
		MenuItem deleteMenu = new MenuItem("删除", new ImageView("/image/16/delete.png"));
		MenuItem detailMenu = new MenuItem("显示详情", new ImageView("/image/16/detail.png"));
		MenuItem copyURLMenu = new MenuItem("复制下载地址", new ImageView("/image/16/download.png"));
		MenuItem exportTorrentMenu = new MenuItem("导出种子", new ImageView("/image/16/export.png"));
		MenuItem openFolderMenu = new MenuItem("打开目录", new ImageView("/image/16/folder.png"));

		startMenu.setOnAction((event) -> {
			mainController.handleStartAction(event);
		});
		
		this.getItems().add(startMenu);
		this.getItems().add(pauseMenu);
		this.getItems().add(deleteMenu);
		this.getItems().add(detailMenu);
		this.getItems().add(copyURLMenu);
		this.getItems().add(exportTorrentMenu);
		this.getItems().add(openFolderMenu);
	}
	
}
