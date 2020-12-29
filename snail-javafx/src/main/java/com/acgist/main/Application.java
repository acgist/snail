package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.Snail;
import com.acgist.snail.context.SystemContext;
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
		SystemContext.build(); // 初始化系统上下文
		if(Snail.available()) {
			 // 初始化GUI
			GuiManager.getInstance()
				.register(ShowEvent.getInstance())
				.register(HideEvent.getInstance())
				.register(ExitEvent.getInstance())
				.register(BuildEvent.getInstance())
				.register(AlertEvent.getInstance())
				.register(NoticeEvent.getInstance())
				.register(TorrentEvent.getInstance())
				.register(ResponseEvent.getInstance())
				.register(RefreshTaskListEvent.getInstance())
				.register(RefreshTaskStatusEvent.getInstance())
				.init(args)
				.build();
		} else {
			LOGGER.debug("启动系统失败");
		}
		LOGGER.info("系统启动完成");
	}

}