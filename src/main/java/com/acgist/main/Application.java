package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.system.context.SystemContext;

/**
 * <h1>Snail系统启动类。</h1>
 * <p>Snail（蜗牛）是一款下载软件，支持下载协议：BT（BitTorrent）、FTP、HTTP。</p>
 * <p>启动参数：{@linkplain GuiHandler args[0]}</p>
 * 
 * TODO：添加注释、日志优化
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	/**
	 * 启动
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
	 * 启动系统监听
	 * 
	 * @return 启动结果：true-成功；false-失败；
	 */
	private static final boolean listen() {
		return SystemContext.listen();
	}
	
	/**
	 * 初始化系统上下文
	 */
	private static final void buildContext() {
		SystemContext.init();
	}
	
	/**
	 * 初始化窗口
	 * 
	 * @param args 启动参数
	 */
	private static final void buildWindow(String ... args) {
		GuiHandler.getInstance().init(args).build();
	}

}