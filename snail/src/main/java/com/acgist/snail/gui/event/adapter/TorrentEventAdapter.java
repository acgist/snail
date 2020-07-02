package com.acgist.snail.gui.event.adapter;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;
import com.acgist.snail.gui.event.GuiEventExtend;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.system.format.BEncodeDecoder;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>GUI种子文件选择事件</p>
 * <p>不能抛出异常：抛出异常会导致创建任务不能正常的删除临时文件</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class TorrentEventAdapter extends GuiEventExtend {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentEventAdapter.class);
	
	protected TorrentEventAdapter() {
		super(Type.TORRENT, "种子文件选择事件");
	}

	@Override
	protected final void executeExtend(GuiManager.Mode mode, Object ... args) {
		if(args == null) {
			LOGGER.warn("种子文件选择（参数错误）：{}", args);
		} else if(args.length == 1) {
			final Object object = args[0];
			if(object instanceof ITaskSession) {
				if(mode == Mode.NATIVE) {
					this.executeNativeExtend((ITaskSession) object);
				} else {
					this.executeExtendExtend((ITaskSession) object);
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
	protected void executeNativeExtend(ITaskSession taskSession) {
		this.executeExtendExtend(taskSession);
	}
	
	/**
	 * <p>扩展GUI</p>
	 * 
	 * @param taskSession 任务信息
	 */
	protected void executeExtendExtend(ITaskSession taskSession) {
		final String files = GuiManager.getInstance().files();
		if(StringUtils.isEmpty(files)) {
			LOGGER.debug("种子文件选择没有文件信息：{}", files);
			return;
		}
		try {
			final var decoder = BEncodeDecoder.newInstance(files);
			// 选择文件列表
			final var selectFiles = decoder.nextList().stream()
				.map(object -> StringUtils.getString(object))
				.collect(Collectors.toList());
			final var torrent = TorrentManager.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
			// 选择文件大小
			final long size = torrent.getInfo().files().stream()
				.filter(file -> !file.path().startsWith(TorrentInfo.PADDING_FILE_PREFIX))
				.filter(file -> selectFiles.contains(file.path()))
				.collect(Collectors.summingLong(TorrentFile::getLength));
			taskSession.setSize(size);
			taskSession.setDescription(files);
		} catch (Exception e) {
			LOGGER.error("设置种子文件选择异常：{}", files, e);
		}
	}
	
}
