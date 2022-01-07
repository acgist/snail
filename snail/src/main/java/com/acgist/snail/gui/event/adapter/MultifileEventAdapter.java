package com.acgist.snail.gui.event.adapter;

import java.util.List;
import java.util.stream.Collectors;

import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.GuiContext.Mode;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.gui.event.GuiEventArgs;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.wrapper.DescriptionWrapper;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>GUI选择下载文件事件</p>
 * 
 * @author acgist
 */
public class MultifileEventAdapter extends GuiEventArgs {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultifileEventAdapter.class);
	
	public MultifileEventAdapter() {
		super(Type.MULTIFILE, "选择下载文件事件");
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
		String files = GuiContext.getInstance().files();
		try {
			List<String> selectFiles;
			final var torrent = TorrentContext.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
			if(StringUtils.isEmpty(files)) {
				// 没有选择文件默认下载所有文件
				selectFiles = torrent.getInfo().files().stream()
					.filter(TorrentFile::notPaddingFile)
					.map(TorrentFile::path)
					.collect(Collectors.toList());
				files = DescriptionWrapper.newEncoder(selectFiles).serialize();
			} else {
				// 选择文件列表
				selectFiles = DescriptionWrapper.newDecoder(files).deserialize().stream()
					.map(StringUtils::getString)
					.collect(Collectors.toList());
			}
			// 选择文件大小
			final long size = torrent.getInfo().files().stream()
				.filter(file -> selectFiles.contains(file.path()))
				.collect(Collectors.summingLong(TorrentFile::getLength));
			taskSession.setSize(size);
			taskSession.setDescription(files);
		} catch (DownloadException e) {
			LOGGER.error("设置选择下载文件异常：{}", files, e);
		} finally {
			GuiContext.getInstance().files(null);
		}
	}
	
}
