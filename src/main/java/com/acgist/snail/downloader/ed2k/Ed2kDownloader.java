package com.acgist.snail.downloader.ed2k;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.pojo.session.TaskSession;

/**
 * <p>ED2K下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Ed2kDownloader extends Downloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ed2kDownloader.class);
	
	private Ed2kDownloader(TaskSession taskSession) {
		super(taskSession);
	}

	public static final Ed2kDownloader newInstance(TaskSession taskSession) {
		return new Ed2kDownloader(taskSession);
	}

	@Override
	public void open() {
	}

	@Override
	public void download() throws IOException {
		LOGGER.debug("ED2K任务开始下载");
	}

	@Override
	public void release() {
	}

}
