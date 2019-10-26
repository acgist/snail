package com.acgist.snail.protocol.thunder;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>迅雷协议</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ThunderProtocol extends Protocol {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThunderProtocol.class);
	
	private static final ThunderProtocol INSTANCE = new ThunderProtocol();

	private ThunderProtocol() {
		super(Type.THUNDER);
	}
	
	public static final ThunderProtocol getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String name() {
		return "迅雷链接";
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(TaskSession taskSession) {
		return null;
	}
	
	@Override
	protected Protocol convert() throws DownloadException {
		final String prefix = Protocol.Type.THUNDER.prefix(this.url);
		final String url = this.url.substring(prefix.length());
		String realUrl = new String(Base64.getMimeDecoder().decode(url)); // getMimeDecoder防止长度非4的整数倍导致的异常
		realUrl = realUrl.substring(2, realUrl.length() - 2);
		LOGGER.debug("迅雷原始链接：{}", realUrl);
		return ProtocolManager.getInstance().protocol(realUrl);
	}
	
	@Override
	protected void cleanMessage(boolean ok) {
	}

}
