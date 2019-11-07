package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.system.context.SystemContext;

/**
 * <h1>Snail系统启动类</h1>
 * <p>Snail（蜗牛）是一款下载软件，支持下载协议：BT（BitTorrent）、FTP、HTTP。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	/**
	 * <p>启动系统<p>
	 * <table border="1">
	 * 	<tr>
	 * 		<th>启动参数</th>
	 * 		<th align="left">功能</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>args[0]</td>
	 * 		<td><strong>gui</strong>：本地GUI；daemo：后台模式；</td>
	 * 	</tr>
	 * </table>
	 * 
	 * @param args 启动参数
	 */
	public static final void main(String[] args) {
		LOGGER.info("系统开始启动");
		SystemContext.info();
		final boolean enable = listen();
		if(enable) {
			buildContext();
			buildWindow(args);
		}
		LOGGER.info("系统启动完成");
	}
	
	/**
	 * <p>启动系统监听</p>
	 * 
	 * @return true-成功；false-失败；
	 */
	private static final boolean listen() {
		return SystemContext.listen();
	}
	
	/**
	 * <p>初始化系统上下文</p>
	 */
	private static final void buildContext() {
		SystemContext.init();
	}
	
	/**
	 * <p>初始化GUI窗口</p>
	 * 
	 * @param args 启动参数
	 */
	private static final void buildWindow(String ... args) {
		GuiHandler.getInstance().init(args).build();
	}

}