package com.acgist.snail.gui.javafx;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.javafx.event.AlertEvent;
import com.acgist.snail.gui.javafx.event.BuildEvent;
import com.acgist.snail.gui.javafx.event.ExitEvent;
import com.acgist.snail.gui.javafx.event.HideEvent;
import com.acgist.snail.gui.javafx.event.NoticeEvent;
import com.acgist.snail.gui.javafx.event.RefreshTaskListEvent;
import com.acgist.snail.gui.javafx.event.RefreshTaskStatusEvent;
import com.acgist.snail.gui.javafx.event.ResponseEvent;
import com.acgist.snail.gui.javafx.event.ShowEvent;
import com.acgist.snail.gui.javafx.event.TorrentEvent;

/**
 * <p>JavaFX GUI管理器</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class JavaFXGuiManager {

	private static final JavaFXGuiManager INSTANCE = new JavaFXGuiManager();
	
	public static final JavaFXGuiManager getInstance() {
		return INSTANCE;
	}
	
	private JavaFXGuiManager() {
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
