package com.acgist.snail.context.initializer.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.initializer.Initializer;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>初始化下载器</p>
 * 
 * @author acgist
 * 
 * TODO：加载所有任务
 */
public final class DownloaderInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private DownloaderInitializer() {
	}
	
	public static final DownloaderInitializer newInstance() {
		return new DownloaderInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化下载器");
		final EntityContext entityContext = EntityContext.getInstance();
		final List<TaskEntity> list = entityContext.allTask();
		if(CollectionUtils.isNotEmpty(list)) {
			list.forEach(entity -> {
				try {
					final var taskSession = TaskSession.newInstance(entity);
					DownloaderManager.getInstance().submit(taskSession);
				} catch (DownloadException e) {
					LOGGER.error("添加下载任务异常：{}", entity.getName(), e);
					entityContext.delete(entity);
				}
			});
			// 刷新下载
			DownloaderManager.getInstance().refresh();
			// 刷新状态
			GuiManager.getInstance().refreshTaskList();
		}
	}

}
