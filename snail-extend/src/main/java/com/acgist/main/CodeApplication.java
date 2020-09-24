package com.acgist.main;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.gui.extend.ExtendGuiManager;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.exception.DownloadException;
import com.acgist.snail.gui.GuiManager;
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
				args = new String[] { "mode=extend" };
			}
			ExtendGuiManager.getInstance().registerEvent(); // 注册事件
			createTaskAsyn();
			GuiManager.getInstance().init(args).build(); // 初始化GUI
		} else {
			LOGGER.debug("启动监听失败");
		}
		LOGGER.info("系统启动完成");
	}

	/**
	 * <p>异步创建任务</p>
	 */
	private static final void createTaskAsyn() {
		SystemThreadContext.timer(4, TimeUnit.SECONDS, () -> {
			try {
				// 单个文件任务
				DownloaderManager.getInstance().newTask("下载地址");
				// BT任务
//				GuiManager.getInstance().files("B编码下载文件列表"); // 设置选择文件
//				DownloaderManager.getInstance().newTask("种子文件路径"); // 开始下载任务
			} catch (DownloadException e) {
				LOGGER.error("下载异常", e);
			}
		});
	}
	
}
