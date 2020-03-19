package com.acgist.snail.gui.event.impl;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;
import com.acgist.snail.gui.event.GuiEventEx;
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
 * <p>GUI种子文件选择事件</p>
 * <p>不能抛出异常：抛出异常会导致{@link TorrentProtocol}创建任务不能正常的删除临时文件</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class TorrentEvent extends GuiEventEx {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentEvent.class);
	
	private static final TorrentEvent INSTANCE = new TorrentEvent();
	
	/**
	 * <p>种子文件选择列表（B编码）</p>
	 */
	private String files;
	
	protected TorrentEvent() {
		super(Type.TORRENT, "种子文件选择事件");
	}
	
	public static final TorrentEvent getInstance() {
		return INSTANCE;
	}

	@Override
	protected void executeEx(GuiManager.Mode mode, Object ... args) {
		if(args == null) {
			LOGGER.warn("种子文件选择（参数错误）：{}", args);
		} else if(args.length == 1) {
			final Object object = args[0];
			if(object instanceof ITaskSession) {
				if(mode == Mode.NATIVE) {
					executeNativeEx((ITaskSession) object);
				} else {
					executeExtendEx((ITaskSession) object);
				}
			} else {
				LOGGER.warn("种子文件选择（参数类型错误）：{}", object);
			}
		} else {
			LOGGER.warn("种子文件选择（参数长度错误）：{}", args);
		}
	}
	
	/**
	 * <p>本地GUI</p>
	 * 
	 * @param taskSession 任务信息
	 */
	private void executeNativeEx(ITaskSession taskSession) {
		if(Platform.isFxApplicationThread()) { // JavaFX线程：本地GUI
			TorrentWindow.getInstance().show(taskSession);
		} else { // 非JavaFX线程：扩展GUI
			executeExtendEx(taskSession);
		}
	}
	
	/**
	 * <p>扩展GUI</p>
	 * 
	 * @param taskSession 任务信息
	 */
	private void executeExtendEx(ITaskSession taskSession) {
		if(StringUtils.isEmpty(this.files)) {
			LOGGER.debug("种子文件选择没有文件信息：{}", this.files);
			return;
		}
		try {
			final var decoder = BEncodeDecoder.newInstance(this.files);
			// 选择文件列表
			final var selectFiles = decoder.nextList().stream()
				.map(object -> BEncodeDecoder.getString(object))
				.collect(Collectors.toList());
			final var torrent = TorrentManager.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
			// 选择文件大小
			final long size = torrent.getInfo().files().stream()
				.filter(file -> !file.path().startsWith(TorrentInfo.PADDING_FILE_PREFIX))
				.filter(file -> selectFiles.contains(file.path()))
				.collect(Collectors.summingLong(TorrentFile::getLength));
			taskSession.setSize(size);
			taskSession.setDescription(this.files);
		} catch (Exception e) {
			LOGGER.error("设置种子文件选择异常：{}", this.files, e);
		}
	}
	
	/**
	 * <p>设置种子文件选择列表</p>
	 * 
	 * @param files 种子文件选择列表（B编码）
	 */
	public void files(String files) {
		this.files = files;
	}

}
