package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.system.context.SystemContext;

/**
 * <p>Snail启动类</p>
 * 
 * @author acgist
 * @since 1.0.0
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
		final boolean enable = SystemContext.listen(); // 启动系统监听
		if(enable) {
			SystemContext.init(); // 初始化系统上下文
			GuiManager.getInstance().init(args).build(); // 初始化GUI
		} else {
			LOGGER.debug("启动监听失败");
		}
		LOGGER.info("系统启动完成");
	}

}