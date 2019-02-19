package com.acgist.killer.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Spring上下文工具
 */
public class SpringContextUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringContextUtils.class);
	
	private static volatile ApplicationContext context;

	/**
	 * 初始化上下文
	 */
	public static final void init(ApplicationContext context) {
		LOGGER.info("初始化Spring上下文");
		SpringContextUtils.context = context;
	}
	
	/**
	 * 自旋
	 */
	private static final void spinning() {
		if(context != null) {
			return;
		}
		synchronized (SpringContextUtils.class) {
			do {
				if(context == null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOGGER.info("自旋异常", e);
					}
				} else {
					break;
				}
			} while(true);
		}
	}
	
	/**
	 * 获取Bean
	 * @param clazz 类型
	 */
	public static final <T> T getBean(Class<T> clazz) {
		spinning();
		return context.getBean(clazz);
	}

}
