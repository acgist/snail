package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.adapter.TorrentEventAdapter;
import com.acgist.snail.gui.javafx.torrent.TorrentWindow;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.protocol.torrent.TorrentProtocol;

import javafx.application.Platform;

/**
 * <p>GUI种子文件选择事件</p>
 * <p>不能抛出异常：抛出异常会导致{@link TorrentProtocol}创建任务不能正常的删除临时文件</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class TorrentEvent extends TorrentEventAdapter {

	private static final TorrentEvent INSTANCE = new TorrentEvent();
	
	public static final TorrentEvent getInstance() {
		return INSTANCE;
	}

	@Override
	protected final void executeNativeExtend(ITaskSession taskSession) {
		if(Platform.isFxApplicationThread()) { // JavaFX线程：本地GUI
			TorrentWindow.getInstance().show(taskSession);
		} else { // 非JavaFX线程：扩展GUI
			this.executeExtendExtend(taskSession);
		}
	}
	
}
