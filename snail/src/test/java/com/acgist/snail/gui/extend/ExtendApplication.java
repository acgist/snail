package com.acgist.snail.gui.extend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.utils.ArrayUtils;

/**
 * <p>Snail启动类</p>
 * <p>启动后台模式</p>
 * 
 * @author acgist
 */
public class ExtendApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendApplication.class);
	
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
			GuiManager.getInstance().init(args).build(); // 初始化GUI
		} else {
			LOGGER.debug("启动监听失败");
		}
		LOGGER.info("系统启动完成");
	}

}
