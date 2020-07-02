package com.acgist.gui.extend;

import com.acgist.gui.extend.event.AlertEvent;
import com.acgist.gui.extend.event.BuildEvent;
import com.acgist.gui.extend.event.ExitEvent;
import com.acgist.gui.extend.event.HideEvent;
import com.acgist.gui.extend.event.NoticeEvent;
import com.acgist.gui.extend.event.RefreshTaskListEvent;
import com.acgist.gui.extend.event.RefreshTaskStatusEvent;
import com.acgist.gui.extend.event.ShowEvent;
import com.acgist.gui.extend.event.TorrentEvent;
import com.acgist.snail.gui.GuiManager;

/**
 * <p>JavaFX GUI管理器</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class ExtendGuiManager {

	private static final ExtendGuiManager INSTANCE = new ExtendGuiManager();
	
	private ExtendGuiManager() {
	}
	
	public static final ExtendGuiManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>注册GUI事件</p>
	 */
	public void registerEvent() {
		GuiManager.register(BuildEvent.getInstance());
		GuiManager.register(ShowEvent.getInstance());
		GuiManager.register(HideEvent.getInstance());
		GuiManager.register(ExitEvent.getInstance());
		GuiManager.register(AlertEvent.getInstance());
		GuiManager.register(NoticeEvent.getInstance());
		GuiManager.register(TorrentEvent.getInstance());
		GuiManager.register(RefreshTaskListEvent.getInstance());
		GuiManager.register(RefreshTaskStatusEvent.getInstance());
	}
	
}
