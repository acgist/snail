package com.acgist.snail.system.initializer.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderFactory;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.protocol.ProtocolManager;
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
		ProtocolManager.getInstance().available();
		LOGGER.info("初始化下载器");
		TaskRepository repository = new TaskRepository();
		List<TaskEntity> list = repository.findAll();
		if(CollectionUtils.isNotEmpty(list)) {
			list
			.stream()
			.map(entity -> {
				try {
					return DownloaderFactory.newInstance(entity);
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
			TaskDisplay.getInstance().refreshTaskTable();
		}
	}
	
}
