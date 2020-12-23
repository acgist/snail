package com.acgist.snail.protocol.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>HTTP协议</p>
 * 
 * @author acgist
 */
public final class HttpProtocol extends Protocol {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpProtocol.class);
	
	private static final HttpProtocol INSTANCE = new HttpProtocol();
	
	public static final HttpProtocol getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>获取HTTP下载响应头的最大重试次数：{@value}</p>
	 */
	private static final int HTTP_HEADER_RETRY_MAX_TIMES = 3;
	
	/**
	 * <p>HTTP头部信息</p>
	 */
	private HttpHeaderWrapper httpHeaderWrapper;
	
	private HttpProtocol() {
		super(Type.HTTP);
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
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return HttpDownloader.newInstance(taskSession);
	}

	@Override
	protected void prep() throws DownloadException {
		this.buildHttpHeader();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>优先使用头部信息中的文件名称</p>
	 */
	@Override
	protected String buildFileName() throws DownloadException {
		String fileName = this.httpHeaderWrapper.fileName(null);
		// 获取失败使用默认名称
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
	protected void release(boolean success) {
		super.release(success);
		this.httpHeaderWrapper = null;
	}

	/**
	 * <p>获取HTTP头部信息</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void buildHttpHeader() throws DownloadException {
		int index = 0;
		while(index++ < HTTP_HEADER_RETRY_MAX_TIMES) {
			try {
				this.httpHeaderWrapper = HTTPClient.newInstance(this.url).head();
			} catch (NetException e) {
				LOGGER.error("获取HTTP头部信息异常：{}", index, e);
			}
			if(this.httpHeaderWrapper != null && this.httpHeaderWrapper.isNotEmpty()) {
				break;
			}
		}
		if(this.httpHeaderWrapper == null || this.httpHeaderWrapper.isEmpty()) {
			throw new DownloadException("获取HTTP头部信息失败");
		}
	}
	
}
