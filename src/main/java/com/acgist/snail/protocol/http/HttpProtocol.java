package com.acgist.snail.protocol.http;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.net.http.HttpManager;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * HTTP解析器
 */
public class HttpProtocol extends Protocol {

	public static final String HTTP_REGEX = "http://.+";
	public static final String HTTPS_REGEX = "https://.+";
	
	public static final String HTTP_PREFIX = "http://";
	public static final String HTTPS_PREFIX = "https://";

	private HttpHeaderWrapper httpHeaderWrapper;
	
	private HttpProtocol() {
		super(Type.http, HTTP_REGEX, HTTPS_REGEX);
	}
	
	@Override
	public String name() {
		return "HTTP";
	}

	@Override
	public boolean available() {
		return true;
	}

	@Override
	protected void buildTaskEntity() throws DownloadException {
		buildHeader();
		TaskEntity taskEntity = new TaskEntity();
		String fileName = buildFileName(); // 文件名称
		taskEntity.setUrl(this.url);
		taskEntity.setType(this.type);
		taskEntity.setStatus(Status.await);
		taskEntity.setName(buildName(fileName));
		taskEntity.setFile(buildFile(fileName));
		taskEntity.setFileType(FileUtils.fileType(fileName));
		taskEntity.setSize(buildSize());
		this.taskEntity = taskEntity;
	}

	@Override
	protected IDownloader buildDownloader() {
		return HttpDownloader.newInstance(this.taskWrapper);
	}

	@Override
	protected void cleanMessage() {
		httpHeaderWrapper = null;
	}

	/**
	 * 获取下载响应头
	 */
	private void buildHeader() throws DownloadException {
		int index = 0;
		while(true) {
			index++;
			this.httpHeaderWrapper = HttpManager.httpHeader(url);
			if(this.httpHeaderWrapper.isNotEmpty()) {
				break;
			}
			if(index >= 3) {
				break;
			}
		}
		if(httpHeaderWrapper.isEmpty()) {
			throw new DownloadException("添加下载任务异常");
		}
	}
	
	@Override
	protected String buildFileName() {
		String fileName = httpHeaderWrapper.fileName(null);
		if(StringUtils.isEmpty(fileName)) {
			fileName = super.buildFileName();
		}
		return fileName;
	}

	private long buildSize() {
		return httpHeaderWrapper.fileSize();
	}

}
