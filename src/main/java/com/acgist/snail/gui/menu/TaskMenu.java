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
import com.acgist.snail.gui.Fonts.SnailIcon;
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
 * <p>任务菜单</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TaskMenu extends Menu {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskMenu.class);
	
	private static final TaskMenu INSTANCE;
	
	static {
		LOGGER.debug("初始化任务菜单");
		INSTANCE = new TaskMenu();
	}
	
	private TaskMenu() {
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
		// 创建按钮
		this.startMenu = buildMenuItem("开始", SnailIcon.PLAY3);
		this.pauseMenu = buildMenuItem("暂停", SnailIcon.PAUSE2);
		this.deleteMenu = buildMenuItem("删除", SnailIcon.BIN);
		this.copyUrlMenu = buildMenuItem("复制链接", SnailIcon.LINK);
		this.torrentMenu = buildMenuItem("文件选择", SnailIcon.EQUALIZER);
		this.exportTorrentMenu = buildMenuItem("导出种子", SnailIcon.SHARE);
		this.verifyMenu = buildMenuItem("文件校验", SnailIcon.CHECKMARK);
		this.openFolderMenu = buildMenuItem("打开目录", SnailIcon.FOLDER_OPEN);
		// 设置按钮事件
		this.startMenu.setOnAction(this.startEvent);
		this.pauseMenu.setOnAction(this.pauseEvent);
		this.deleteMenu.setOnAction(this.deleteEvent);
		this.copyUrlMenu.setOnAction(this.copyUrlEvent);
		this.torrentMenu.setOnAction(this.torrentEvent);
		this.exportTorrentMenu.setOnAction(this.exportTorrentEvent);
		this.verifyMenu.setOnAction(this.verifyEvent);
		this.openFolderMenu.setOnAction(this.openFolderEvent);
		// 添加按钮
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
		// 窗口显示事件
		this.addEventFilter(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件捕获阶段处理事件
//		this.addEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件冒泡阶段处理事件
//		this.setEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件冒泡阶段处理事件（只能存在一个）
	}
	
	/**
	 * <p>开始</p>
	 */
	private EventHandler<ActionEvent> startEvent = (event) -> {
		MainWindow.getInstance().controller().start();
	};
	
	/**
	 * <p>暂停</p>
	 */
	private EventHandler<ActionEvent> pauseEvent = (event) -> {
		MainWindow.getInstance().controller().pause();
	};
	
	/**
	 * <p>删除</p>
	 */
	private EventHandler<ActionEvent> deleteEvent = (event) -> {
		MainWindow.getInstance().controller().delete();
	};
	
	/**
	 * <p>复制链接</p>
	 */
	private EventHandler<ActionEvent> copyUrlEvent = (event) -> {
		MainWindow.getInstance().controller().selected().forEach(session -> {
			Clipboards.copy(session.getUrl());
		});
	};
	
	/**
	 * <p>文件选择</p>
	 */
	private EventHandler<ActionEvent> torrentEvent = (event) -> {
		if(!MainWindow.getInstance().controller().haveSelectedTorrent()) {
			return;
		}
		MainWindow.getInstance().controller().selected().forEach(session -> {
			if(session.getType() == Type.TORRENT) {
				TorrentWindow.getInstance().controller().tree(session);
			}
		});
		TorrentWindow.getInstance().show();
	};
	
	/**
	 * <p>导出种子</p>
	 */
	private EventHandler<ActionEvent> exportTorrentEvent = (event) -> {
		if(!MainWindow.getInstance().controller().haveSelectedTorrent()) {
			return;
		}
		final File file = Choosers.chooseDirectory(MainWindow.getInstance().stage(), "种子保存目录");
		if (file != null) {
			MainWindow.getInstance().controller().selected().forEach(session -> {
				if(session.getType() == Type.TORRENT) {
					final String torrent = session.getTorrent();
					final String fileName = FileUtils.fileNameFromUrl(torrent);
					final String newFile = FileUtils.file(file.getPath(), fileName);
					FileUtils.copy(torrent, newFile);
				}
			});
		}
	};
	
	/**
	 * <p>文件校验</p>
	 */
	private EventHandler<ActionEvent> verifyEvent = (event) -> {
		SystemThreadContext.submit(() -> {
			final Map<String, String> hash = new HashMap<>();
			MainWindow.getInstance().controller().selected().forEach(session -> {
				if(session.complete()) {
					hash.putAll(FileUtils.sha1(session.getFile()));
				}
			});
			if(hash.isEmpty()) {
				Platform.runLater(() -> {
					Alerts.warn("校验失败", "请等待任务下载完成");
				});
			} else {
				Platform.runLater(() -> {
					final StringBuilder builder = new StringBuilder();
					hash.forEach((key, value) -> {
						builder.append(value).append("=").append(FileUtils.fileNameFromUrl(key)).append("\n");
					});
					final Optional<ButtonType> optional = Alerts.info("文件SHA-1校验", builder.toString());
					if(optional.isPresent() && ButtonType.OK == optional.get()) {
						// 点击确认自动复制信息
						Clipboards.copy(builder.toString());
					}
				});
			}
		});
	};
	
	/**
	 * <p>打开目录</p>
	 */
	private EventHandler<ActionEvent> openFolderEvent = (event) -> {
		MainWindow.getInstance().controller().selected().forEach(session -> {
			FileUtils.openInDesktop(session.downloadFolder());
		});
	};
	
	/**
	 * <p>窗口显示时如果选中任务中有BT任务时显示按钮：文件选择、导出种子</p>
	 */
	private EventHandler<WindowEvent> windowShownAction = (event) -> {
		if(MainWindow.getInstance().controller().haveSelectedTorrent()) {
			INSTANCE.torrentMenu.setDisable(false);
			INSTANCE.exportTorrentMenu.setDisable(false);
		} else {
			INSTANCE.torrentMenu.setDisable(true);
			INSTANCE.exportTorrentMenu.setDisable(true);
		}
	};
	
}
