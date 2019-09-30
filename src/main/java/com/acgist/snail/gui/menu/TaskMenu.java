package com.acgist.snail.gui.menu;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Alerts;
import com.acgist.snail.gui.Choosers;
import com.acgist.snail.gui.Clipboards;
import com.acgist.snail.gui.Menu;
import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.gui.torrent.TorrentWindow;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.FileUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.WindowEvent;

/**
 * 菜单 - 任务
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TaskMenu extends Menu {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskMenu.class);
	
	private static final TaskMenu INSTANCE;
	
	static {
		LOGGER.debug("初始化任务菜单");
		INSTANCE = new TaskMenu();
	}
	
	private TaskMenu() {
		init();
		initMenu();
	}
	
	public static final TaskMenu getInstance() {
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
	protected void initMenu() {
		this.startMenu = buildMenuItem("开始", "/image/16/start.png");
		this.pauseMenu = buildMenuItem("暂停", "/image/16/pause.png");
		this.deleteMenu = buildMenuItem("删除", "/image/16/delete.png");
		this.copyUrlMenu = buildMenuItem("复制地址", "/image/16/download.png");
		this.torrentMenu = buildMenuItem("文件选择", "/image/16/edit.png");
		this.exportTorrentMenu = buildMenuItem("导出种子", "/image/16/export.png");
		this.verifyMenu = buildMenuItem("文件校验", "/image/16/verify.png");
		this.openFolderMenu = buildMenuItem("打开目录", "/image/16/folder.png");
		
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
		
		this.addEventFilter(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件捕获阶段处理事件
//		this.addEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件冒泡阶段处理事件
//		this.setEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件冒泡阶段处理事件（只能有一个）
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
		MainWindow.getInstance().controller().selected().forEach(session -> {
			Clipboards.copy(session.entity().getUrl());
		});
	};
	
	private EventHandler<ActionEvent> torrentEvent = (event) -> {
		if(!MainWindow.getInstance().controller().hasSelectedTorrent()) {
			return;
		}
		MainWindow.getInstance().controller().selected().forEach(session -> {
			if(session.entity().getType() == Type.torrent) {
				TorrentWindow.getInstance().controller().tree(session);
			}
		});
		TorrentWindow.getInstance().show();
	};
	
	private EventHandler<ActionEvent> exportTorrentEvent = (event) -> {
		if(!MainWindow.getInstance().controller().hasSelectedTorrent()) {
			return;
		}
		final File file = Choosers.chooseDirectory(MainWindow.getInstance().stage(), "种子文件保存目录");
		if (file != null) {
			MainWindow.getInstance().controller().selected().forEach(session -> {
				if(session.entity().getType() == Type.torrent) {
					String torrent = session.entity().getTorrent();
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
			MainWindow.getInstance().controller().selected().forEach(session -> {
				if(session.complete()) {
					hash.putAll(FileUtils.sha1(session.entity().getFile()));
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
						Clipboards.copy(builder.toString());
					}
				});
			}
		});
	};
	
	private EventHandler<ActionEvent> openFolderEvent = (event) -> {
		MainWindow.getInstance().controller().selected().forEach(session -> {
			FileUtils.openInDesktop(session.downloadFolder());
		});
	};
	
	/**
	 * BT任务才可以选择下载文件和导出种子
	 */
	private EventHandler<WindowEvent> windowShownAction = (event) -> {
		if(MainWindow.getInstance().controller().hasSelectedTorrent()) {
			INSTANCE.torrentMenu.setDisable(false);
			INSTANCE.exportTorrentMenu.setDisable(false);
		} else {
			INSTANCE.torrentMenu.setDisable(true);
			INSTANCE.exportTorrentMenu.setDisable(true);
		}
//		event.consume(); // 事件已被处理
	};
	
}
