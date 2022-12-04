package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.gui.event.adapter.MultifileEventAdapter;
import com.acgist.snail.gui.javafx.window.torrent.TorrentWindow;

import javafx.application.Platform;

/**
 * <p>GUI选择下载文件事件</p>
 * 
 * @author acgist
 */
public final class MultifileEvent extends MultifileEventAdapter {

	private static final MultifileEvent INSTANCE = new MultifileEvent();
	
	public static final MultifileEvent getInstance() {
		return INSTANCE;
	}
	
	private MultifileEvent() {
	}

	@Override
	protected void executeNativeExtend(ITaskSession taskSession) {
		if(Platform.isFxApplicationThread()) {
			// JavaFX线程：本地GUI
			TorrentWindow.getInstance().show(taskSession);
		} else {
			// 非JavaFX线程：扩展GUI
			this.executeExtendExtend(taskSession);
		}
	}
	
}
