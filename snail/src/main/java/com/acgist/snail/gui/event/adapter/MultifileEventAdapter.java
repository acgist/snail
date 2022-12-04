package com.acgist.snail.gui.event.adapter;

import java.util.List;
import java.util.stream.Collectors;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.wrapper.DescriptionWrapper;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.GuiContext.Mode;
import com.acgist.snail.gui.event.GuiEventArgs;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.application.ApplicationMessage;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentFile;
import com.acgist.snail.utils.ModifyOptional;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>GUI选择下载文件事件</p>
 * 
 * @author acgist
 */
public class MultifileEventAdapter extends GuiEventArgs {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultifileEventAdapter.class);
	
	/**
	 * 选择下载文件列表（B编码）
	 */
	private static final ModifyOptional<String> FILES = ModifyOptional.newInstance();
	
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
		String files = FILES.get();
		try {
			final var torrent = TorrentContext.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
			// 没有选择文件
			while(StringUtils.isEmpty((files = FILES.get()))) {
				final String allFiles = DescriptionWrapper.newEncoder(
					torrent.getInfo().files().stream()
					.filter(TorrentFile::notPaddingFile)
					.map(TorrentFile::path)
					.collect(Collectors.toList())
				).serialize();
				final ApplicationMessage message = ApplicationMessage.Type.MULTIFILE.build(allFiles);
				GuiContext.getInstance().sendExtendGuiMessage(message);
				synchronized (FILES) {
					try {
						FILES.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.error("文件选择等待异常", e);
					}
				}
			}
			// 选择文件列表
			final List<String> selectFiles = DescriptionWrapper.newDecoder(files).deserialize().stream()
				.map(StringUtils::getString)
				.collect(Collectors.toList());
			// 选择文件大小
			final long size = torrent.getInfo().files().stream()
				.filter(file -> selectFiles.contains(file.path()))
				.collect(Collectors.summingLong(TorrentFile::getLength));
			taskSession.setSize(size);
			taskSession.setDescription(files);
		} catch (DownloadException e) {
			LOGGER.error("设置选择下载文件异常：{}", files, e);
		} finally {
			FILES.delete();
		}
	}
	
	/**
	 * 设置选择下载文件列表（B编码）
	 * 
	 * @param files 选择下载文件列表（B编码）
	 */
	public static final void files(String files) {
		FILES.set(files);
		synchronized (FILES) {
			FILES.notifyAll();
		}
	}
	
}
