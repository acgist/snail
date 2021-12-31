package com.acgist.snail.protocol.hls;

import java.net.URI;

import com.acgist.snail.config.SymbolConfig.Symbol;
import com.acgist.snail.context.HlsContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.hls.HlsDownloader;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.M3u8;
import com.acgist.snail.pojo.wrapper.DescriptionWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>HLS协议</p>
 * <p>协议链接：https://tools.ietf.org/html/rfc8216</p>
 * 
 * @author acgist
 */
public final class HlsProtocol extends Protocol {

	private static final HlsProtocol INSTANCE = new HlsProtocol();
	
	/**
	 * <p>默认结尾：{@value}</p>
	 */
	private static final String INDEX_M3U8 = "/index.m3u8";
	
	public static final HlsProtocol getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>M3U8信息</p>
	 */
	private M3u8 m3u8;
	
	private HlsProtocol() {
		super(Type.HLS, "HLS");
	}

	@Override
	public boolean available() {
		return true;
	}

	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return HlsDownloader.newInstance(taskSession);
	}
	
	@Override
	protected void prep() throws DownloadException {
		try {
			this.buildM3u8();
		} catch (NetException e) {
			throw new DownloadException("网络异常", e);
		}
	}
	
	@Override
	protected String buildFileName() throws DownloadException {
		final String path = URI.create(this.url).getPath();
		if(StringUtils.endsWithIgnoreCase(path, INDEX_M3U8)) {
			// 去掉斜杠和结尾
			return path.substring(1, path.length() - INDEX_M3U8.length())
				.replace(Symbol.SLASH.toChar(), Symbol.MINUS.toChar()) + 
				Protocol.Type.HLS.defaultSuffix();
		} else {
			return super.buildFileName();
		}
	}
	
	@Override
	protected void buildSize() throws DownloadException {
		this.taskEntity.setSize(0L);
	}
	
	@Override
	protected void done() throws DownloadException {
		this.buildFolder();
		this.selectFiles();
	}

	/**
	 * <p>获取M3U8信息</p>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	private void buildM3u8() throws NetException, DownloadException {
		final var response = HttpClient
			.newInstance(this.url)
			.get()
			.responseToString();
		final var m3u8Check = M3u8Builder.newInstance(response, this.url).build();
		if(m3u8Check.getType() == M3u8.Type.M3U8) {
			this.url = m3u8Check.maxRateLink();
			this.buildM3u8();
		} else if(m3u8Check.getType() == M3u8.Type.STREAM) {
			throw new DownloadException("不支持直播流媒体下载");
		} else {
			this.m3u8 = m3u8Check;
		}
	}
	
	/**
	 * <p>新建下载目录</p>
	 */
	private void buildFolder() {
		FileUtils.buildFolder(this.taskEntity.getFile());
	}
	
	/**
	 * <p>保持下载文件列表</p>
	 */
	private void selectFiles() {
		// M3U8协议默认下载所有文件
		this.taskEntity.setDescription(DescriptionWrapper.newEncoder(this.m3u8.getLinks()).serialize());
	}
	
	@Override
	protected void success() {
		// 成功添加管理
		HlsContext.getInstance().m3u8(this.taskEntity.getId(), this.m3u8);
	}
	
	@Override
	protected void release(boolean success) {
		this.m3u8 = null;
		super.release(success);
	}

}
