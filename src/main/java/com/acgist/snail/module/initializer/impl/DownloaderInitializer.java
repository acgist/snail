package com.acgist.snail.module.initializer.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderBuilder;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.module.initializer.Initializer;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.window.main.TaskTimer;

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
			list
			.stream()
			.map(entity -> {
				try {
					return DownloaderBuilder.newBuilder(entity);
				} catch (DownloadException e) {
					LOGGER.error("添加下载任务异常", e);
				}
				return null;
			})
			.filter(builder -> builder != null)
			.forEach(builder -> {
				try {
					builder.build();
				} catch (DownloadException e) {
					var wrapper = builder.wrapper();
					if(wrapper != null) {
						var entity = wrapper.entity();
						wrapper.delete();
						LOGGER.info("删除下载任务：{}", entity.getName());
					}
					LOGGER.error("添加下载任务异常", e);
				}
			});
			TaskTimer.getInstance().refreshTaskTable();
		}
	}
	
}
