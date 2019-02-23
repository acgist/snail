package com.acgist.snail.downloader;

import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.module.config.FileTypeConfig.FileType;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * 下载器构建
 */
public class DownloadBuilder {

	public static final void build(String path) {
		TaskEntity entity = new TaskEntity("test", Type.http, FileType.image, "", "", "", Status.await, 10);
		entity.setId("xx");
		TaskWrapper wrapper = new TaskWrapper(entity);
		DownloaderManager.getInstance().download(new HttpDownloader(wrapper));
	}
	
}
