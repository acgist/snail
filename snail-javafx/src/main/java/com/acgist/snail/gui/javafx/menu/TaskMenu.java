package com.acgist.snail.gui.javafx.menu;

import java.io.File;
import java.util.Optional;

import com.acgist.snail.context.GuiContext;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Choosers;
import com.acgist.snail.gui.javafx.Clipboards;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.Fonts.SnailIcon;
import com.acgist.snail.gui.javafx.window.main.MainWindow;
import com.acgist.snail.gui.javafx.window.torrent.TorrentWindow;
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
 */
public final class TaskMenu extends Menu {

	private static final TaskMenu INSTANCE;
	
	public static final TaskMenu getInstance() {
		return INSTANCE;
	}
	
	static {
		INSTANCE = new TaskMenu();
	}

	/**
	 * <p>文件选择按钮</p>
	 */
	private MenuItem torrentMenu;
	/**
	 * <p>导出种子按钮</p>
	 */
	private MenuItem exportTorrentMenu;
	
	private TaskMenu() {
		this.buildMenus();
	}
	
	@Override
	protected void buildMenus() {
		// 开始按钮
		this.buildMenuItem("开始", SnailIcon.AS_PLAY3, this.startEvent);
		// 暂停按钮
		this.buildMenuItem("暂停", SnailIcon.AS_PAUSE2, this.pauseEvent);
		// 删除按钮
		this.buildMenuItem("删除", SnailIcon.AS_BIN, this.deleteEvent);
		// 复制链接按钮
		this.buildMenuItem("复制链接", SnailIcon.AS_LINK, this.copyUrlEvent);
		// 分割线
		this.buildSeparator();
		// 文件选择按钮
		this.torrentMenu = this.buildMenuItem("文件选择", SnailIcon.AS_EQUALIZER, this.torrentEvent);
		// 导出种子按钮
		this.exportTorrentMenu = this.buildMenuItem("导出种子", SnailIcon.AS_SHARE, this.exportTorrentEvent);
		// 分割线
		this.buildSeparator();
		// 文件校验按钮
		this.buildMenuItem("文件校验", SnailIcon.AS_CHECKMARK, this.verifyEvent);
		// 打开目录按钮
		this.buildMenuItem("打开目录", SnailIcon.AS_FOLDER_OPEN, this.openFolderEvent);
		// 窗口显示事件
		// 事件捕获阶段处理事件
		this.addEventFilter(WindowEvent.WINDOW_SHOWN, this.windowShownAction);
		// 事件冒泡阶段处理事件
//		this.addEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownAction);
		// 事件冒泡阶段处理事件（只能存在一个）
//		this.setEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownAction);
	}
	
	/**
	 * <p>开始</p>
	 */
	private EventHandler<ActionEvent> startEvent = event -> MainWindow.getInstance().controller().start();
	
	/**
	 * <p>暂停</p>
	 */
	private EventHandler<ActionEvent> pauseEvent = event -> MainWindow.getInstance().controller().pause();
	
	/**
	 * <p>删除</p>
	 */
	private EventHandler<ActionEvent> deleteEvent = event -> MainWindow.getInstance().controller().delete();
	
	/**
	 * <p>复制链接</p>
	 */
	private EventHandler<ActionEvent> copyUrlEvent = event -> MainWindow.getInstance().controller().selected().forEach(
		session -> Clipboards.copy(session.getUrl())
	);
	
	/**
	 * <p>文件选择</p>
	 */
	private EventHandler<ActionEvent> torrentEvent = event -> {
		if(!MainWindow.getInstance().controller().hasSelectedTorrent()) {
			return;
		}
		MainWindow.getInstance().controller().selectedTorrent().forEach(TorrentWindow.getInstance()::show);
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
			final String exportPath = file.getAbsolutePath();
			MainWindow.getInstance().controller().selectedTorrent().forEach(session -> {
				final String torrent = session.getTorrent();
				final String fileName = FileUtils.fileName(torrent);
				final String targetFile = FileUtils.file(exportPath, fileName);
				FileUtils.copy(torrent, targetFile);
			});
		}
	};
	
	/**
	 * <p>文件校验</p>
	 */
	private EventHandler<ActionEvent> verifyEvent = event -> {
		if(!MainWindow.getInstance().controller().hasSelected()) {
			return;
		}
		Platform.runLater(() -> MainWindow.getInstance().controller().selected().forEach(session -> {
			if(session.verify()) {
				Alerts.info("校验成功", session.getName());
			} else if(session.statusCompleted()) {
				// 任务完成：判断是否需要重新下载
				final Optional<ButtonType> optional = Alerts.build("校验失败", "是否重新下载任务？", GuiContext.MessageType.CONFIRM);
				if(Alerts.ok(optional)) {
					session.repause();
				}
			} else {
				Alerts.warn("校验失败", "开始下载自动修复");
			}
		}));
	};
	
	/**
	 * <p>打开目录</p>
	 */
	private EventHandler<ActionEvent> openFolderEvent = event -> MainWindow.getInstance().controller().selected().forEach(session -> {
		final File folder = session.downloadFolder();
		if(folder.exists()) {
			Desktops.open(folder);
		} else {
			Alerts.warn("打开失败", "下载文件已经删除");
		}
	});
	
	/**
	 * <p>有选中BT任务时按钮可以操作：文件选择、导出种子</p>
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
