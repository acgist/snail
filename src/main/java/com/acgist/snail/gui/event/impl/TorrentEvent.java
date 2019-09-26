package com.acgist.snail.gui.event.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.torrent.TorrentWindow;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.utils.StringUtils;

import javafx.application.Platform;

/**
 * <p>GUI种子文件选择事件</p>
 * <p>不能抛出异常</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class TorrentEvent extends GuiEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentEvent.class);
	
	private static final TorrentEvent INSTANCE = new TorrentEvent();
	
	/**
	 * 被选中的下载文件列表（B编码）
	 */
	private String files;
	
	protected TorrentEvent() {
		super(Type.torrent, "种子文件选择事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		executeEx(true, args);
	}

	@Override
	protected void executeExtend(Object ... args) {
		executeEx(false, args);
	}
	
	private void executeEx(boolean gui, Object ... args) {
		if(args == null || args.length < 1) {
			LOGGER.error("种子文件选择参数错误：{}", args);
		} else {
			final Object object = args[0];
			if(object instanceof TaskSession) {
				if(gui) {
					executeNativeEx((TaskSession) object);
				} else {
					executeExtendEx((TaskSession) object);
				}
			} else {
				LOGGER.error("种子文件选择参数错误：{}", object);
			}
		}
	}
	
	private void executeNativeEx(TaskSession taskSession) {
		if(Platform.isFxApplicationThread()) { // JavaFX线程
			TorrentWindow.getInstance().show(taskSession);
		} else { // 非JavaFX线程：外部GUI
			executeExtendEx(taskSession);
		}
	}
	
	private void executeExtendEx(TaskSession taskSession) {
		try {
			if(StringUtils.isEmpty(this.files)) {
				return;
			}
			final var entity = taskSession.entity();
			// 选择文件
			final List<String> files = BEncodeDecoder.newInstance(this.files).nextList().stream()
				.map(object -> new String((byte[]) object))
				.collect(Collectors.toList());
			final Torrent torrent = TorrentManager.getInstance().newTorrentSession(entity.getTorrent()).torrent();
			// 选择文件大小
			final long size = torrent.getInfo().files().stream()
				.filter(file -> !file.path().startsWith(TorrentInfo.HIDE_FILE_PREFIX))
				.filter(file -> files.contains(file.path()))
				.collect(Collectors.summingLong(TorrentFile::getLength));
			entity.setSize(size);
			entity.setDescription(this.files);
		} catch (Exception e) {
			LOGGER.error("添加下载任务异常：{}", this.files, e);
		}
	}
	
	public void files(String files) {
		this.files = files;
	}
	
	public static final TorrentEvent getInstance() {
		return INSTANCE;
	}

}
