package com.acgist.main;

import com.acgist.snail.Snail;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.javafx.event.AlertEvent;
import com.acgist.snail.gui.javafx.event.BuildEvent;
import com.acgist.snail.gui.javafx.event.ExitEvent;
import com.acgist.snail.gui.javafx.event.HideEvent;
import com.acgist.snail.gui.javafx.event.MultifileEvent;
import com.acgist.snail.gui.javafx.event.NoticeEvent;
import com.acgist.snail.gui.javafx.event.RefreshTaskListEvent;
import com.acgist.snail.gui.javafx.event.RefreshTaskStatusEvent;
import com.acgist.snail.gui.javafx.event.ResponseEvent;
import com.acgist.snail.gui.javafx.event.ShowEvent;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>Snail启动类</p>
 * 
 * @author acgist
 */
public final class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	/**
	 * <p>启动方法</p>
	 * 
	 * <table border="1">
	 * 	<caption>启动参数</caption>
	 * 	<tr>
	 * 		<th>参数</th>
	 * 		<th>默认</th>
	 * 		<th>描述</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code mode}</td>
	 * 		<td>{@code native}</td>
	 * 		<td>{@code native}-本地GUI；{@code extend}-扩展GUI；</td>
	 * 	</tr>
	 * </table>
	 * 
	 * @param args 启动参数
	 */
	public static final void main(String[] args) {
		LOGGER.info("系统开始启动");
		SystemContext.info();
		SystemContext.build();
		if(Snail.available()) {
			Application.registerGuiEvent();
			GuiContext.getInstance().init(args).build();
			LOGGER.info("系统启动完成");
		} else {
			LOGGER.info("系统启动失败");
		}
	}
	
	/**
	 * <p>注册GUI事件</p>
	 */
	private static final void registerGuiEvent() {
		GuiContext.register(ShowEvent.getInstance());
		GuiContext.register(HideEvent.getInstance());
		GuiContext.register(ExitEvent.getInstance());
		GuiContext.register(BuildEvent.getInstance());
		GuiContext.register(AlertEvent.getInstance());
		GuiContext.register(NoticeEvent.getInstance());
		GuiContext.register(ResponseEvent.getInstance());
		GuiContext.register(MultifileEvent.getInstance());
		GuiContext.register(RefreshTaskListEvent.getInstance());
		GuiContext.register(RefreshTaskStatusEvent.getInstance());
	}

}