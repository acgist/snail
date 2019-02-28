package com.acgist.snail.window.menu;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.ClipboardUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.window.edit.EditWindow;
import com.acgist.snail.window.main.MainWindow;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * 任务菜单
 */
public class TaskMenu extends ContextMenu {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskMenu.class);
	
	private static TaskMenu INSTANCE;
	
	private TaskMenu() {
		createMenu();
	}

	static {
		synchronized (TaskMenu.class) {
			if (INSTANCE == null) {
				LOGGER.info("初始化任务菜单");
				INSTANCE = new TaskMenu();
			}
		}
	}
	
	public static final TaskMenu getInstance() {
		return INSTANCE;
	}

	/**
	 * 创建菜单
	 */
	private void createMenu() {
		MenuItem startMenu = new MenuItem("开始", new ImageView("/image/16/start.png"));
		MenuItem pauseMenu = new MenuItem("暂停", new ImageView("/image/16/pause.png"));
		MenuItem deleteMenu = new MenuItem("删除", new ImageView("/image/16/delete.png"));
		MenuItem editMenu = new MenuItem("任务编辑", new ImageView("/image/16/edit.png"));
		MenuItem copyURLMenu = new MenuItem("复制下载地址", new ImageView("/image/16/download.png"));
		MenuItem exportTorrentMenu = new MenuItem("导出种子", new ImageView("/image/16/export.png"));
		MenuItem openFolderMenu = new MenuItem("打开目录", new ImageView("/image/16/folder.png"));

		startMenu.setOnAction((event) -> {
			MainWindow.getInstance().controller().start();
		});
		
		pauseMenu.setOnAction((event) -> {
			MainWindow.getInstance().controller().pause();
		});
		
		deleteMenu.setOnAction((event) -> {
			MainWindow.getInstance().controller().delete();
		});
		
		editMenu.setOnAction((event) -> {
			List<TaskWrapper> list = MainWindow.getInstance().controller().selected();
			if(CollectionUtils.isEmpty(list)) {
				return;
			}
			list.forEach(wrapper -> {
				EditWindow.getInstance().controller().tree(wrapper);
			});
			EditWindow.getInstance().show();
		});
		
		copyURLMenu.setOnAction((event) -> {
			List<TaskWrapper> list = MainWindow.getInstance().controller().selected();
			list.forEach(wrapper -> {
				ClipboardUtils.copy(wrapper.getUrl());
			});
		});
		
		exportTorrentMenu.setOnAction((event) -> {
			List<TaskWrapper> list = MainWindow.getInstance().controller().selected();
			if(CollectionUtils.isEmpty(list)) {
				return;
			}
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("文件保存目录");
			File file = chooser.showDialog(new Stage());
			if (file != null) {
				list.forEach(wrapper -> {
					if(wrapper.getType() == Type.torrent) {
						String torrent = wrapper.getTorrent();
						String fileName = FileUtils.fileNameFromUrl(torrent);
						String newFile = FileUtils.file(file.getPath(), fileName);
						FileUtils.copy(torrent, newFile);
					}
				});
			}
		});
		
		openFolderMenu.setOnAction((event) -> {
			List<TaskWrapper> list = MainWindow.getInstance().controller().selected();
			list.forEach(wrapper -> {
				FileUtils.openInDesktop(wrapper.getFileFolder());
			});
		});
		
		this.getItems().add(startMenu);
		this.getItems().add(pauseMenu);
		this.getItems().add(deleteMenu);
		this.getItems().add(editMenu);
		this.getItems().add(copyURLMenu);
		this.getItems().add(exportTorrentMenu);
		this.getItems().add(openFolderMenu);
	}
	
}
