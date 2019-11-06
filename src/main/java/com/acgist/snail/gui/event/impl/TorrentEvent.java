package com.acgist.snail.gui.event.impl;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.torrent.TorrentWindow;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.utils.StringUtils;

import javafx.application.Platform;

/**
 * <p>种子文件选择事件</p>
 * <p>不能抛出异常：抛出异常后{@link TorrentProtocol}创建下载时不能正常的删除临时文件</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class TorrentEvent extends GuiEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentEvent.class);
	
	private static final TorrentEvent INSTANCE = new TorrentEvent();
	
	/**
	 * 被选中下载文件列表（B编码）
	 */
	private String files;
	
	protected TorrentEvent() {
		super(Type.TORRENT, "种子文件选择事件");
	}
	
	public static final TorrentEvent getInstance() {
		return INSTANCE;
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
		if(args == null) {
			LOGGER.warn("种子文件选择，参数错误：{}", args);
		} else if(args.length == 1) {
			final Object object = args[0];
			if(object instanceof ITaskSession) {
				if(gui) {
					executeNativeEx((ITaskSession) object);
				} else {
					executeExtendEx((ITaskSession) object);
				}
			} else {
				LOGGER.warn("种子文件选择，参数错误（类型）：{}", object);
			}
		} else {
			LOGGER.warn("种子文件选择，参数错误（长度）：{}", args);
		}
	}
	
	private void executeNativeEx(ITaskSession taskSession) {
		if(Platform.isFxApplicationThread()) { // JavaFX线程
			TorrentWindow.getInstance().show(taskSession);
		} else { // 非JavaFX线程：扩展GUI
			executeExtendEx(taskSession);
		}
	}
	
	private void executeExtendEx(ITaskSession taskSession) {
		if(StringUtils.isEmpty(this.files)) {
			return;
		}
		try {
			final var decoder = BEncodeDecoder.newInstance(this.files);
			// 选择文件列表
			final var files = decoder.nextList().stream()
				.map(object -> BEncodeDecoder.getString(object))
				.collect(Collectors.toList());
			final var torrent = TorrentManager.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
			// 选择文件大小
			final long size = torrent.getInfo().files().stream()
				.filter(file -> !file.path().startsWith(TorrentInfo.PADDING_FILE_PREFIX))
				.filter(file -> files.contains(file.path()))
				.collect(Collectors.summingLong(TorrentFile::getLength));
			taskSession.setSize(size);
			taskSession.setDescription(this.files);
		} catch (Exception e) {
			LOGGER.error("设置种子文件选择异常：{}", this.files, e);
		}
	}
	
	/**
	 * 设置种子文件选择列表
	 * 
	 * @param files 种子文件选择列表（B编码）
	 */
	public void files(String files) {
		this.files = files;
	}

}
