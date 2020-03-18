package com.acgist.snail.system.initializer.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.initializer.Initializer;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>初始化下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DownloaderInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderInitializer.class);
	
	private DownloaderInitializer() {
	}
	
	public static final DownloaderInitializer newInstance() {
		return new DownloaderInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化下载器");
		final TaskRepository repository = new TaskRepository();
		final List<TaskEntity> list = repository.findAll();
		if(CollectionUtils.isNotEmpty(list)) {
			list.stream()
				.forEach(entity -> {
					try {
						final var taskSession = TaskSession.newInstance(entity);
						DownloaderManager.getInstance().submit(taskSession);
					} catch (DownloadException e) {
						LOGGER.error("添加下载任务异常：{}", entity.getName(), e);
						repository.delete(entity);
					}
				});
			DownloaderManager.getInstance().refresh(); // 刷新下载
			GuiManager.getInstance().refreshTaskList(); // 刷新状态
		}
	}

}
