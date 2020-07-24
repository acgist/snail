package com.acgist.snail.protocol.hls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.hls.HlsDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>HLS协议</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class HlsProtocol extends Protocol {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsProtocol.class);
	
	private static final HlsProtocol INSTANCE = new HlsProtocol();
	
	public static final HlsProtocol getInstance() {
		return INSTANCE;
	}
	
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
	}

	/**
	 * <p>获取M3U8信息</p>
	 */
	private void buildM3u8() {
	}

}
