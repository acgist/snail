package com.acgist.snail.protocol.hls;

import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.hls.HlsDownloader;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.M3u8;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.hls.bootstrap.M3u8Builder;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>HLS协议</p>
 * <p>协议链接：https://tools.ietf.org/html/rfc8216</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class HlsProtocol extends Protocol {

//	private static final Logger LOGGER = LoggerFactory.getLogger(HlsProtocol.class);
	
	private static final HlsProtocol INSTANCE = new HlsProtocol();
	
	public static final HlsProtocol getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>M3U8信息</p>
	 */
	private M3u8 m3u8;
	
	private HlsProtocol() {
		super(Type.HLS);
	}

	@Override
	public String name() {
		return "HLS";
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
		this.buildM3u8();
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
	 * @throws DownloadException 下载异常
	 */
	private void buildM3u8() throws DownloadException {
		final var client = HTTPClient.newInstance(this.url);
		HttpResponse<String> response;
		try {
			response = client.get(BodyHandlers.ofString());
		} catch (NetException e) {
			throw new DownloadException("获取M3U8信息失败", e);
		}
		final var m3u8 = M3u8Builder.newInstance(response.body(), this.url).build();
		if(m3u8.getType() == M3u8.Type.M3U8) {
			this.url = m3u8.maxRateLink();
			this.buildM3u8();
		} else if(m3u8.getType() == M3u8.Type.STREAM) {
			throw new DownloadException("不支持直播流媒体下载");
		} else {
			this.m3u8 = m3u8;
		}
	}
	
	/**
	 * <p>创建下载目录</p>
	 */
	private void buildFolder() {
		FileUtils.buildFolder(this.taskEntity.getFile(), false);
	}
	
	/**
	 * <p>保持下载文件列表</p>
	 */
	private void selectFiles() {
		final MultifileSelectorWrapper wrapper = MultifileSelectorWrapper.newEncoder(this.m3u8.getLinks());
		this.taskEntity.setDescription(wrapper.serialize());
	}
	
	@Override
	protected void clean(boolean ok) {
		super.clean(ok);
		this.m3u8 = null;
	}

}
