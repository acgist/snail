package com.acgist.snail.protocol.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * HTTP协议
 * 
 * @author acgist
 * @since 1.0.0
 */
public class HttpProtocol extends Protocol {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpProtocol.class);
	
	public static final String HTTP_REGEX = "http://.+";
	public static final String HTTPS_REGEX = "https://.+";
	
	public static final String HTTP_PREFIX = "http://";
	public static final String HTTPS_PREFIX = "https://";

	private HttpHeaderWrapper httpHeaderWrapper;
	
	private static final HttpProtocol INSTANCE = new HttpProtocol();
	
	private HttpProtocol() {
		super(Type.http, HTTP_REGEX, HTTPS_REGEX);
	}
	
	public static final HttpProtocol getInstance() {
		return INSTANCE;
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
	protected boolean buildTaskEntity() throws DownloadException {
		buildHttpHeader();
		final TaskEntity taskEntity = new TaskEntity();
		final String fileName = buildFileName(); // 文件名称
		taskEntity.setUrl(this.url);
		taskEntity.setType(this.type);
		taskEntity.setStatus(Status.await);
		taskEntity.setName(buildName(fileName));
		taskEntity.setFile(buildFile(fileName));
		taskEntity.setFileType(FileUtils.fileType(fileName));
		taskEntity.setSize(buildSize());
		this.taskEntity = taskEntity;
		return true;
	}

	@Override
	protected void cleanMessage() {
		this.httpHeaderWrapper = null;
	}

	/**
	 * 获取下载响应头
	 */
	private void buildHttpHeader() throws DownloadException {
		int index = 0;
		while(true) {
			index++;
			try {
				this.httpHeaderWrapper = HTTPClient.head(url);
			} catch (NetException e) {
				LOGGER.error("HTTP下载请求头获取异常", e);
			}
			if(this.httpHeaderWrapper != null && this.httpHeaderWrapper.isNotEmpty()) {
				break;
			}
			if(index >= 3) {
				break;
			}
		}
		if(this.httpHeaderWrapper == null || this.httpHeaderWrapper.isEmpty()) {
			throw new DownloadException("添加下载任务异常");
		}
	}
	
	@Override
	protected String buildFileName() {
		String fileName = this.httpHeaderWrapper.fileName(null);
		if(StringUtils.isEmpty(fileName)) {
			fileName = super.buildFileName();
		}
		return fileName;
	}

	private long buildSize() {
		return this.httpHeaderWrapper.fileSize();
	}
	
	/**
	 * 验证HTTP协议
	 */
	public static final boolean verify(String url) {
		return
			StringUtils.regex(url, HTTP_REGEX, true) ||
			StringUtils.regex(url, HTTPS_REGEX, true);
	}

}
