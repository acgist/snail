package com.acgist.snail.gui.extend;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.extend.event.AlertEvent;
import com.acgist.snail.gui.extend.event.BuildEvent;
import com.acgist.snail.gui.extend.event.ExitEvent;
import com.acgist.snail.gui.extend.event.HideEvent;
import com.acgist.snail.gui.extend.event.NoticeEvent;
import com.acgist.snail.gui.extend.event.RefreshTaskListEvent;
import com.acgist.snail.gui.extend.event.RefreshTaskStatusEvent;
import com.acgist.snail.gui.extend.event.ResponseEvent;
import com.acgist.snail.gui.extend.event.ShowEvent;
import com.acgist.snail.gui.extend.event.TorrentEvent;

/**
 * <p>扩展GUI管理器</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class ExtendGuiManager {

	private static final ExtendGuiManager INSTANCE = new ExtendGuiManager();
	
	public static final ExtendGuiManager getInstance() {
		return INSTANCE;
	}
	
	private ExtendGuiManager() {
	}
	
	/**
	 * <p>注册GUI事件</p>
	 */
	public void registerEvent() {
		GuiManager.register(ShowEvent.getInstance());
		GuiManager.register(HideEvent.getInstance());
		GuiManager.register(ExitEvent.getInstance());
		GuiManager.register(BuildEvent.getInstance());
		GuiManager.register(AlertEvent.getInstance());
		GuiManager.register(NoticeEvent.getInstance());
		GuiManager.register(TorrentEvent.getInstance());
		GuiManager.register(ResponseEvent.getInstance());
		GuiManager.register(RefreshTaskListEvent.getInstance());
		GuiManager.register(RefreshTaskStatusEvent.getInstance());
	}
	
}
