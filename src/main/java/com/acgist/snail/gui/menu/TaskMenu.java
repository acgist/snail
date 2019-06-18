package com.acgist.snail.gui.menu;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Alerts;
import com.acgist.snail.gui.Choosers;
import com.acgist.snail.gui.Menu;
import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.gui.torrent.TorrentWindow;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ClipboardUtils;
import com.acgist.snail.utils.FileUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

/**
 * 菜单 - 任务
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TaskMenu extends Menu {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskMenu.class);
	
	private static TaskMenu INSTANCE;
	
	private TaskMenu() {
		init();
		buildMenu();
	}

	static {
		synchronized (TaskMenu.class) {
			if (INSTANCE == null) {
				LOGGER.debug("初始化任务菜单");
				INSTANCE = new TaskMenu();
			}
		}
	}
	
	public static final TaskMenu getInstance() {
		if(MainWindow.getInstance().controller().haveTorrent()) {
			INSTANCE.torrentMenu.setDisable(false);
			INSTANCE.exportTorrentMenu.setDisable(false);
		} else {
			INSTANCE.torrentMenu.setDisable(true);
			INSTANCE.exportTorrentMenu.setDisable(true);
		}
		return INSTANCE;
	}

	private MenuItem startMenu;
	private MenuItem pauseMenu;
	private MenuItem deleteMenu;
	private MenuItem copyUrlMenu;
	private MenuItem torrentMenu;
	private MenuItem exportTorrentMenu;
	private MenuItem verifyMenu;
	private MenuItem openFolderMenu;
	
	@Override
	protected void buildMenu() {
		this.startMenu = new MenuItem("开始", new ImageView("/image/16/start.png"));
		this.pauseMenu = new MenuItem("暂停", new ImageView("/image/16/pause.png"));
		this.deleteMenu = new MenuItem("删除", new ImageView("/image/16/delete.png"));
		this.copyUrlMenu = new MenuItem("复制地址", new ImageView("/image/16/download.png"));
		this.torrentMenu = new MenuItem("文件选择", new ImageView("/image/16/edit.png"));
		this.exportTorrentMenu = new MenuItem("导出种子", new ImageView("/image/16/export.png"));
		this.verifyMenu = new MenuItem("文件校验", new ImageView("/image/16/verify.png"));
		this.openFolderMenu = new MenuItem("打开目录", new ImageView("/image/16/folder.png"));
		
		this.startMenu.setOnAction(this.startEvent);
		this.pauseMenu.setOnAction(this.pauseEvent);
		this.deleteMenu.setOnAction(this.deleteEvent);
		this.copyUrlMenu.setOnAction(this.copyUrlEvent);
		this.torrentMenu.setOnAction(this.torrentEvent);
		this.exportTorrentMenu.setOnAction(this.exportTorrentEvent);
		this.verifyMenu.setOnAction(this.verifyEvent);
		this.openFolderMenu.setOnAction(this.openFolderEvent);
		
		addMenu(this.startMenu);
		addMenu(this.pauseMenu);
		addMenu(this.deleteMenu);
		addMenu(this.copyUrlMenu);
		this.addSeparator();
		addMenu(this.torrentMenu);
		addMenu(this.exportTorrentMenu);
		this.addSeparator();
		addMenu(this.verifyMenu);
		addMenu(this.openFolderMenu);
	}
	
	private EventHandler<ActionEvent> startEvent = (event) -> {
		MainWindow.getInstance().controller().start();
	};
	
	private EventHandler<ActionEvent> pauseEvent = (event) -> {
		MainWindow.getInstance().controller().pause();
	};
	
	private EventHandler<ActionEvent> deleteEvent = (event) -> {
		MainWindow.getInstance().controller().delete();
	};
	
	private EventHandler<ActionEvent> copyUrlEvent = (event) -> {
		MainWindow.getInstance().controller().selected()
		.forEach(wrapper -> {
			ClipboardUtils.copy(wrapper.entity().getUrl());
		});
	};
	
	private EventHandler<ActionEvent> torrentEvent = (event) -> {
		if(!MainWindow.getInstance().controller().haveTorrent()) {
			return;
		}
		MainWindow.getInstance().controller().selected()
		.forEach(wrapper -> {
			if(wrapper.entity().getType() == Type.torrent) {
				TorrentWindow.getInstance().controller().tree(wrapper);
			}
		});
		TorrentWindow.getInstance().show();
	};
	
	private EventHandler<ActionEvent> exportTorrentEvent = (event) -> {
		if(!MainWindow.getInstance().controller().haveTorrent()) {
			return;
		}
		final File file = Choosers.chooseDirectory(MainWindow.getInstance().stage(), "种子文件保存目录");
		if (file != null) {
			MainWindow.getInstance().controller().selected()
			.forEach(wrapper -> {
				if(wrapper.entity().getType() == Type.torrent) {
					String torrent = wrapper.entity().getTorrent();
					String fileName = FileUtils.fileNameFromUrl(torrent);
					String newFile = FileUtils.file(file.getPath(), fileName);
					FileUtils.copy(torrent, newFile);
				}
			});
		}
	};
	
	private EventHandler<ActionEvent> verifyEvent = (event) -> {
		SystemThreadContext.submit(() -> {
			Map<String, String> hash = new HashMap<>();
			MainWindow.getInstance().controller().selected()
			.forEach(wrapper -> {
				if(wrapper.complete()) {
					hash.putAll(FileUtils.sha1(wrapper.entity().getFile()));
				}
			});
			if(hash.isEmpty()) {
				Platform.runLater(() -> {
					Alerts.warn("校验失败", "请等待任务下载完成");
				});
			} else {
				Platform.runLater(() -> {
					StringBuilder builder = new StringBuilder();
					hash.forEach((key, value) -> {
						builder.append(value).append("=").append(FileUtils.fileNameFromUrl(key)).append("\n");
					});
					final Optional<ButtonType> optional = Alerts.info("文件SHA-1校验", builder.toString());
					if(optional.isPresent() && ButtonType.OK == optional.get()) {
						ClipboardUtils.copy(builder.toString());
					}
				});
			}
		});
	};
	
	private EventHandler<ActionEvent> openFolderEvent = (event) -> {
		MainWindow.getInstance().controller().selected()
		.forEach(wrapper -> {
			FileUtils.openInDesktop(wrapper.downloadFolder());
		});
	};
	
}
