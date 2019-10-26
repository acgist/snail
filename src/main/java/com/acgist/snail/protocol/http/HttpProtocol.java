package com.acgist.snail.protocol.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * HTTP协议
 * 
 * @author acgist
 * @since 1.0.0
 */
public class HttpProtocol extends Protocol {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpProtocol.class);
	
	private static final HttpProtocol INSTANCE = new HttpProtocol();

	/**
	 * 获取HTTP下载响应头的最大重试次数
	 */
	private static final int HTTP_HEADER_RETRY_MAX_TIMES = 3;

	private HttpHeaderWrapper httpHeaderWrapper;
	
	private HttpProtocol() {
		super(Type.HTTP);
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
	public IDownloader buildDownloader(TaskSession taskSession) {
		return HttpDownloader.newInstance(taskSession);
	}

	@Override
	protected void prep() throws DownloadException {
		buildHttpHeader();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>优先使用请求头中的文件名称</p>
	 */
	@Override
	protected String buildFileName() throws DownloadException {
		String fileName = this.httpHeaderWrapper.fileName(null);
		if(StringUtils.isEmpty(fileName)) {
			fileName = super.buildFileName();
		}
		return fileName;
	}

	@Override
	protected void buildSize() {
		this.taskEntity.setSize(this.httpHeaderWrapper.fileSize());
	}
	
	@Override
	protected void cleanMessage(boolean ok) {
		this.httpHeaderWrapper = null;
	}

	/**
	 * <p>获取HTTP下载响应头</p>
	 */
	private void buildHttpHeader() throws DownloadException {
		int index = 0;
		while(true) {
			index++;
			try {
				this.httpHeaderWrapper = HTTPClient.newInstance(this.url).head();
			} catch (NetException e) {
				LOGGER.error("获取HTTP下载响应头异常", e);
			}
			if(this.httpHeaderWrapper != null && this.httpHeaderWrapper.isNotEmpty()) {
				break;
			}
			if(index >= HTTP_HEADER_RETRY_MAX_TIMES) {
				break;
			}
		}
		if(this.httpHeaderWrapper == null || this.httpHeaderWrapper.isEmpty()) {
			throw new DownloadException("获取HTTP下载响应头失败");
		}
	}
	
}
