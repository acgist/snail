package com.acgist.snail.gui.event.adapter;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.GuiContext.Mode;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.gui.event.GuiEventArgs;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>GUI种子文件选择事件</p>
 * 
 * @author acgist
 */
public class TorrentEventAdapter extends GuiEventArgs {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentEventAdapter.class);
	
	public TorrentEventAdapter() {
		super(Type.TORRENT, "种子文件选择事件");
	}

	@Override
	protected final void executeExtend(GuiContext.Mode mode, Object ... args) {
		if(!this.check(args, 1)) {
			return;
		}
		final ITaskSession taskSession = (ITaskSession) this.getArg(args, 0);
		if(mode == Mode.NATIVE) {
			this.executeNativeExtend(taskSession);
		} else {
			this.executeExtendExtend(taskSession);
		}
	}
	
	/**
	 * <p>本地消息</p>
	 * 
	 * @param taskSession 任务信息
	 */
	protected void executeNativeExtend(ITaskSession taskSession) {
		this.executeExtendExtend(taskSession);
	}
	
	/**
	 * <p>扩展消息</p>
	 * 
	 * @param taskSession 任务信息
	 */
	protected void executeExtendExtend(ITaskSession taskSession) {
		final String files = GuiContext.getInstance().files();
		if(StringUtils.isEmpty(files)) {
			LOGGER.debug("种子文件选择没有文件信息：{}", files);
			return;
		}
		try {
			final var decoder = BEncodeDecoder.newInstance(files);
			final var torrent = TorrentContext.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
			// 选择文件列表
			final var selectFiles = decoder.nextList().stream()
				.map(StringUtils::getString)
				.collect(Collectors.toList());
			// 选择文件大小
			final long size = torrent.getInfo().files().stream()
				// 设置选择下载文件
				.filter(file -> selectFiles.contains(file.path()))
				.collect(Collectors.summingLong(TorrentFile::getLength));
			taskSession.setSize(size);
			taskSession.setDescription(files);
		} catch (DownloadException | PacketSizeException e) {
			LOGGER.error("设置种子文件选择异常：{}", files, e);
		}
	}
	
}
