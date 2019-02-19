package com.acgist.killer.window.menu;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class TaskMenu extends ContextMenu {

	private static TaskMenu INSTANCE = null;

	private TaskMenu() {
		MenuItem startMenu = new MenuItem("开始");
		MenuItem pauseMenu = new MenuItem("暂停");
		MenuItem deleteMenu = new MenuItem("删除");
		MenuItem copyURLMenu = new MenuItem("复制下载地址");
		MenuItem exportTorrentMenu = new MenuItem("导出种子");
		MenuItem openFolderMenu = new MenuItem("打开目录");

		this.getItems().add(startMenu);
		this.getItems().add(pauseMenu);
		this.getItems().add(deleteMenu);
		this.getItems().add(copyURLMenu);
		this.getItems().add(exportTorrentMenu);
		this.getItems().add(openFolderMenu);
	}

	public static final TaskMenu getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TaskMenu();
		}
		return INSTANCE;
	}

}
