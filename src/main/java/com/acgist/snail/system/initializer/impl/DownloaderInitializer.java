package com.acgist.snail.system.initializer.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.initializer.Initializer;
import com.acgist.snail.utils.CollectionUtils;

/**
 * 初始化：下载器
 */
public class DownloaderInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderInitializer.class);
	
	private DownloaderInitializer() {
	}
	
	public static final DownloaderInitializer newInstance() {
		return new DownloaderInitializer();
	}
	
	public void init() {
		LOGGER.info("初始化下载器");
		TaskRepository repository = new TaskRepository();
		List<TaskEntity> list = repository.findAll();
		if(CollectionUtils.isNotEmpty(list)) {
			list.stream()
			.forEach(entity -> {
				TaskSession session = null;
				try {
					session = TaskSession.newInstance(entity);
					DownloaderManager.getInstance().submit(session);
				} catch (DownloadException e) {
					LOGGER.error("添加下载任务异常", e);
					repository.delete(entity);
				}
			});
			TaskDisplay.getInstance().refreshTaskTable();
		}
	}

}
