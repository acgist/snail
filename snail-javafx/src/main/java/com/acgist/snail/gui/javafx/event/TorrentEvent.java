package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.adapter.TorrentEventAdapter;
import com.acgist.snail.gui.javafx.torrent.TorrentWindow;
import com.acgist.snail.pojo.ITaskSession;

import javafx.application.Platform;

/**
 * <p>GUI种子文件选择事件</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class TorrentEvent extends TorrentEventAdapter {

	private static final TorrentEvent INSTANCE = new TorrentEvent();
	
	public static final TorrentEvent getInstance() {
		return INSTANCE;
	}
	
	private TorrentEvent() {
	}

	@Override
	protected void executeNativeExtend(ITaskSession taskSession) {
		if(Platform.isFxApplicationThread()) { // JavaFX线程：本地GUI
			TorrentWindow.getInstance().show(taskSession);
		} else { // 非JavaFX线程：扩展GUI
			this.executeExtendExtend(taskSession);
		}
	}
	
}
