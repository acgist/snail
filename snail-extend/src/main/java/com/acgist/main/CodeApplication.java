package com.acgist.main;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.gui.extend.ExtendGuiManager;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ArrayUtils;

/**
 * <p>Snail启动类</p>
 * <p>直接使用代码进行任务下载</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public class CodeApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(CodeApplication.class);
	
	/**
	 * <p>启动方法</p>
	 * 
	 * @param args 启动参数
	 */
	public static void main(String[] args) {
		LOGGER.info("系统开始启动");
		SystemContext.info();
		final boolean enable = SystemContext.listen(); // 启动系统监听
		if(enable) {
			SystemContext.init(); // 初始化系统上下文
			// 后台模式启动
			if(ArrayUtils.isEmpty(args)) {
				args = new String[] {"mode=extend"};
			}
			// 注册事件
			ExtendGuiManager.getInstance().registerEvent();
			SystemThreadContext.timer(4, TimeUnit.SECONDS, () -> {
				try {
					// 单个文件任务
//					DownloaderManager.getInstance().newTask("https://mirror.bit.edu.cn/apache/tomcat/tomcat-9/v9.0.36/bin/apache-tomcat-9.0.36.zip");
					// BT任务
					GuiManager.getInstance().files("l62:[Nekomoe kissaten][Fruits Basket S2 (2019)][13][720p][CHS].mp4e"); // 设置选择文件
					DownloaderManager.getInstance().newTask("E:\\snail\\extend.torrent"); // 开始下载任务
				} catch (DownloadException e) {
					LOGGER.error("下载异常", e);
				}
			});
			GuiManager.getInstance().init(args).build(); // 初始化GUI
		} else {
			LOGGER.debug("启动监听失败");
		}
		LOGGER.info("系统启动完成");
	}
	
}
