package com.acgist.snail.gui.javafx.menu;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Choosers;
import com.acgist.snail.gui.javafx.Clipboards;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.Fonts.SnailIcon;
import com.acgist.snail.gui.javafx.Menu;
import com.acgist.snail.gui.javafx.window.main.MainWindow;
import com.acgist.snail.gui.javafx.window.torrent.TorrentWindow;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.FileUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.stage.WindowEvent;

/**
 * <p>任务菜单</p>
 * 
 * @author acgist
 */
public final class TaskMenu extends Menu {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskMenu.class);
	
	private static final TaskMenu INSTANCE;
	
	public static final TaskMenu getInstance() {
		return INSTANCE;
	}
	
	static {
		LOGGER.debug("初始化任务菜单");
		INSTANCE = new TaskMenu();
	}

	/**
	 * <p>开始按钮</p>
	 */
	private MenuItem startMenu;
	/**
	 * <p>暂停按钮</p>
	 */
	private MenuItem pauseMenu;
	/**
	 * <p>删除按钮</p>
	 */
	private MenuItem deleteMenu;
	/**
	 * <p>复制链接按钮</p>
	 */
	private MenuItem copyUrlMenu;
	/**
	 * <p>文件选择按钮</p>
	 */
	private MenuItem torrentMenu;
	/**
	 * <p>导出种子按钮</p>
	 */
	private MenuItem exportTorrentMenu;
	/**
	 * <p>文件校验按钮</p>
	 */
	private MenuItem verifyMenu;
	/**
	 * <p>打开目录按钮</p>
	 */
	private MenuItem openFolderMenu;
	
	private TaskMenu() {
		this.initMenu();
	}
	
	@Override
	protected void initMenu() {
		// 创建按钮
		this.startMenu = buildMenuItem("开始", SnailIcon.AS_PLAY3);
		this.pauseMenu = buildMenuItem("暂停", SnailIcon.AS_PAUSE2);
		this.deleteMenu = buildMenuItem("删除", SnailIcon.AS_BIN);
		this.copyUrlMenu = buildMenuItem("复制链接", SnailIcon.AS_LINK);
		this.torrentMenu = buildMenuItem("文件选择", SnailIcon.AS_EQUALIZER);
		this.exportTorrentMenu = buildMenuItem("导出种子", SnailIcon.AS_SHARE);
		this.verifyMenu = buildMenuItem("文件校验", SnailIcon.AS_CHECKMARK);
		this.openFolderMenu = buildMenuItem("打开目录", SnailIcon.AS_FOLDER_OPEN);
		// 设置按钮事件
		this.startMenu.setOnAction(this.startEvent);
		this.pauseMenu.setOnAction(this.pauseEvent);
		this.deleteMenu.setOnAction(this.deleteEvent);
		this.copyUrlMenu.setOnAction(this.copyUrlEvent);
		this.torrentMenu.setOnAction(this.torrentEvent);
		this.exportTorrentMenu.setOnAction(this.exportTorrentEvent);
		this.verifyMenu.setOnAction(this.verifyEvent);
		this.openFolderMenu.setOnAction(this.openFolderEvent);
		// 操作按钮
		this.addMenu(this.startMenu);
		this.addMenu(this.pauseMenu);
		this.addMenu(this.deleteMenu);
		this.addMenu(this.copyUrlMenu);
		// 种子任务按钮
		this.addSeparator();
		this.addMenu(this.torrentMenu);
		this.addMenu(this.exportTorrentMenu);
		// 其他按钮
		this.addSeparator();
		this.addMenu(this.verifyMenu);
		this.addMenu(this.openFolderMenu);
		// 窗口显示事件
		this.addEventFilter(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件捕获阶段处理事件
//		this.addEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件冒泡阶段处理事件
//		this.setEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownAction); // 事件冒泡阶段处理事件（只能存在一个）
	}
	
	/**
	 * <p>开始</p>
	 */
	private EventHandler<ActionEvent> startEvent = event -> {
		MainWindow.getInstance().controller().start();
	};
	
	/**
	 * <p>暂停</p>
	 */
	private EventHandler<ActionEvent> pauseEvent = event -> {
		MainWindow.getInstance().controller().pause();
	};
	
	/**
	 * <p>删除</p>
	 */
	private EventHandler<ActionEvent> deleteEvent = event -> {
		MainWindow.getInstance().controller().delete();
	};
	
	/**
	 * <p>复制链接</p>
	 */
	private EventHandler<ActionEvent> copyUrlEvent = event -> {
		MainWindow.getInstance().controller().selected().forEach(session -> {
			Clipboards.copy(session.getUrl());
		});
	};
	
	/**
	 * <p>文件选择</p>
	 */
	private EventHandler<ActionEvent> torrentEvent = event -> {
		if(!MainWindow.getInstance().controller().hasSelectedTorrent()) {
			return;
		}
		MainWindow.getInstance().controller().selected().forEach(session -> {
			if(session.getType() == Type.TORRENT) {
				TorrentWindow.getInstance().controller().buildTree(session);
			}
		});
		TorrentWindow.getInstance().show();
	};
	
	/**
	 * <p>导出种子</p>
	 */
	private EventHandler<ActionEvent> exportTorrentEvent = event -> {
		if(!MainWindow.getInstance().controller().hasSelectedTorrent()) {
			return;
		}
		final File file = Choosers.chooseDirectory(MainWindow.getInstance().stage(), "种子保存目录");
		if (file != null) {
			MainWindow.getInstance().controller().selected().forEach(session -> {
				if(session.getType() == Type.TORRENT) {
					final String torrent = session.getTorrent();
					final String fileName = FileUtils.fileName(torrent);
					final String newFile = FileUtils.file(file.getAbsolutePath(), fileName);
					FileUtils.copy(torrent, newFile);
				}
			});
		}
	};
	
	/**
	 * <p>文件校验</p>
	 */
	private EventHandler<ActionEvent> verifyEvent = event -> {
		SystemThreadContext.submit(() -> {
			MainWindow.getInstance().controller().selected().forEach(session -> {
				session.verify();
				Platform.runLater(() -> Alerts.info("校验成功", session.getName()));
			});
		});
	};
	
	/**
	 * <p>打开目录</p>
	 */
	private EventHandler<ActionEvent> openFolderEvent = event -> {
		MainWindow.getInstance().controller().selected().forEach(session -> {
			Desktops.open(session.downloadFolder());
		});
	};
	
	/**
	 * <p>窗口显示时如果选中任务中有BT任务时显示按钮：文件选择、导出种子</p>
	 */
	private EventHandler<WindowEvent> windowShownAction = event -> {
		if(MainWindow.getInstance().controller().hasSelectedTorrent()) {
			INSTANCE.torrentMenu.setDisable(false);
			INSTANCE.exportTorrentMenu.setDisable(false);
		} else {
			INSTANCE.torrentMenu.setDisable(true);
			INSTANCE.exportTorrentMenu.setDisable(true);
		}
	};
	
}
